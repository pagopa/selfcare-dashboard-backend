package it.pagopa.selfcare.dashboard.web.config;

import it.pagopa.selfcare.commons.web.config.SecurityConfig;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.web.security.PartyAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Slf4j
@Configuration
class DashboardSecurityConfig extends SecurityConfig {

    private final PartyAuthenticationProvider authenticationProvider;


    @Autowired
    public DashboardSecurityConfig(JwtService jwtService, PartyAuthenticationProvider authenticationProvider) {
        super(jwtService);
        this.authenticationProvider = authenticationProvider;
    }


    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ADMIN > LEGAL\n" +
                "LEGAL > ADMIN_REF\n" +
                "ADMIN_REF > TECH_REF");
        RoleHierarchyAuthoritiesMapper authoritiesMapper = new RoleHierarchyAuthoritiesMapper(roleHierarchy);
        authenticationProvider.setAuthoritiesMapper(authoritiesMapper);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        authenticationManagerBuilder.eraseCredentials(false);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/dashboard/**").hasAuthority("TECH_REF")
                .antMatchers("/institutions/**").hasAuthority("TECH_REF")
                .anyRequest().permitAll();
        super.configure(http);
    }

}