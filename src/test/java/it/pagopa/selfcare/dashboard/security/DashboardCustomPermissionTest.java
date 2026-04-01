package it.pagopa.selfcare.dashboard.security;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardCustomPermissionTest {

    private final DashboardCustomPermission dashboardCustomPermission = new DashboardCustomPermission();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasPermissionARB_shouldReturnFalse_whenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();
        assertFalse(dashboardCustomPermission.hasPermissionARB());
    }

    @Test
    void hasPermissionARB_shouldReturnFalse_whenPrincipalIsNotSelfCareUser() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("NotASelfCareUser", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(dashboardCustomPermission.hasPermissionARB());
    }

    @Test
    void hasPermissionARB_shouldReturnFalse_whenIssuerIsNotPagoPA() {
        SelfCareUser user = SelfCareUser.builder("userId")
                .issuer("no_PagoPa_issuer")
                .build();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(dashboardCustomPermission.hasPermissionARB());
    }

    @Test
    void hasPermissionARB_shouldReturnTrue_whenIssuerIsPagoPA() {
        SelfCareUser user = SelfCareUser.builder("userId")
                .issuer("PAGOPA")
                .build();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(dashboardCustomPermission.hasPermissionARB());
    }

}