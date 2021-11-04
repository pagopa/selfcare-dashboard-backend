package it.pagopa.selfcare.dashboard.web.config;

import it.pagopa.selfcare.commons.web.config.SecurityConfig;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
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

    private static final String ROLE_PREFIX = "";//TODO: remove me

    private final PartyProcessRestClient restClient;


    @Autowired
    public DashboardSecurityConfig(JwtService jwtService, PartyProcessRestClient restClient) {
        super(jwtService);
        this.restClient = restClient;
    }


    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(ROLE_PREFIX + "ADMIN > " + ROLE_PREFIX + "LEGAL\n" +
                ROLE_PREFIX + "LEGAL > " + ROLE_PREFIX + "ADMIN_REF\n" +
                ROLE_PREFIX + "ADMIN_REF > " + ROLE_PREFIX + "TECH_REF");
        RoleHierarchyAuthoritiesMapper authoritiesMapper = new RoleHierarchyAuthoritiesMapper(roleHierarchy);
        PartyAuthenticationProvider authenticationProvider = new PartyAuthenticationProvider(restClient);
        authenticationProvider.setAuthoritiesMapper(authoritiesMapper);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/dashboard/products/**").hasAuthority("TECH_REF")
                .anyRequest().permitAll();
        super.configure(http);
    }

}