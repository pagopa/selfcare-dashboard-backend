package it.pagopa.selfcare.dashboard.web.config;

import it.pagopa.selfcare.dashboard.connector.rest.PartyRestClient;
import it.pagopa.selfcare.dashboard.web.security.JwtAuthenticationFilter;
import it.pagopa.selfcare.dashboard.web.security.JwtService;
import it.pagopa.selfcare.dashboard.web.security.SelfCareAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@PropertySource("classpath:config/jwt.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/favicon.ico",
            "/error"
    };

    private final JwtService jwtService;
    private final PartyRestClient restClient;


    @Autowired
    public SecurityConfig(JwtService jwtService, PartyRestClient restClient) {
        this.jwtService = jwtService;
        this.restClient = restClient;
    }


    @Override
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        mapper.afterPropertiesSet();
        SelfCareAuthenticationProvider authenticationProvider = new SelfCareAuthenticationProvider(restClient);
        authenticationProvider.setAuthoritiesMapper(mapper);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
    }


    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(AUTH_WHITELIST);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // TODO: configure CORS (if required)
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
//        corsConfiguration.setAllowedOrigins(List.of("*"));
//        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PUT","OPTIONS","PATCH", "DELETE"));
//        corsConfiguration.setAllowCredentials(true);
//        corsConfiguration.setExposedHeaders(List.of("Authorization"));
//        http.cors().configurationSource(request -> corsConfiguration)
        http.exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("Unauthorized error: {}: {}", accessDeniedException.getMessage(), request.getRequestURI());
                    response.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    log.error("Unauthorized error: {}: {}", authException.getMessage(), request.getRequestURI());
                    response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"selfcare\"");
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
                })
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/products/**").hasAnyRole("USER", "ADMIN")
//                .antMatchers("/products/**").hasRole("ADMIN")
                .antMatchers("/products/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .cors().and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().disable()
                .addFilterBefore(new JwtAuthenticationFilter(authenticationManagerBean(), jwtService), UsernamePasswordAuthenticationFilter.class);
    }

    // TODO: configure CORS (if required)
//    @Bean
//    protected CorsConfigurationSource corsConfigurationSource() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
//        return source;
//    }

    // TODO: configure default GrantedAuthority (if required)
//    @Bean
//    GrantedAuthorityDefaults grantedAuthorityDefaults() {
//        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
//    }

}