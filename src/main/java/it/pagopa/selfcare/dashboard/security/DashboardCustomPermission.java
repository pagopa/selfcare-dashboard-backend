package it.pagopa.selfcare.dashboard.security;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("dashboardCustomPermission")
public class DashboardCustomPermission {

    private static final String ISSUER_PAGOPA = "PAGOPA";

    public boolean hasPermissionARB() {
        log.info("Start authorization check for ARB");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("Authentication is null");
            return false;
        }

        if (!(authentication.getPrincipal() instanceof SelfCareUser selfCareUser)) {
            log.warn("Authentication principal is not SelfCareUser");
            return false;
        }

        String issuer = selfCareUser.getIssuer();
        boolean result = ISSUER_PAGOPA.equalsIgnoreCase(issuer);

        log.debug("Issuer={}, authorized={}", issuer, result);
        log.info("End authorization check for ARB");

        return result;
    }
}