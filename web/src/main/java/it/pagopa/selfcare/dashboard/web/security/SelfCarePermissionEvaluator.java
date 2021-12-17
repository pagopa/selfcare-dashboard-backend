package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Optional;

public class SelfCarePermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        boolean result = false;

        if (authentication != null && targetType != null) {
            Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .findAny();

            if (selcAuthority.isPresent()) {
                SelfCareGrantedAuthority selfCareGrantedAuthority = (SelfCareGrantedAuthority) selcAuthority.get();

                if (InstitutionResource.class.getSimpleName().equals(targetType)) {
                    result = targetId.equals(selfCareGrantedAuthority.getInstitutionId());

                } else if (ProductsResource.class.getSimpleName().equals(targetType)) {
                    result = selfCareGrantedAuthority.getRoleOnProducts().containsKey(String.valueOf(targetId));
                }
            }
        }

        return result;
    }

}
