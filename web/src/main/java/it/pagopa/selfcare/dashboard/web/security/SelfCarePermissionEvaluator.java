package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.io.Serializable;

@Slf4j
public class SelfCarePermissionEvaluator implements PermissionEvaluator {

    public static final String ANY_PERMISSION = "ANY";


    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.trace("hasPermission start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetDomainObject = {}, permission = {}" , authentication, targetDomainObject, permission);

        Assert.notNull(authentication, "An authentication is required");
        Assert.notNull(permission, "A permission is required");

        boolean result = false;

        if (targetDomainObject != null && ProductAclDomain.class.equals(targetDomainObject.getClass())) {
            ProductAclDomain productAclDomain = (ProductAclDomain) targetDomainObject;
            result = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(SelfCareGrantedAuthority.class::cast)
                    .filter(grantedAuthority -> grantedAuthority.getInstitutionId().equals(productAclDomain.getInstitutionId()))
                    .filter(grantedAuthority -> grantedAuthority.getRoleOnProducts().containsKey(productAclDomain.getProductId()))
                    .map(grantedAuthority -> grantedAuthority.getRoleOnProducts().get(productAclDomain.getProductId()))
                    .anyMatch(grantedAuthority -> ANY_PERMISSION.equals(permission) || permission.equals(grantedAuthority.getAuthority()));
        }

        log.debug("hasPermission result = {}", result);
        log.trace("hasPermission end");
        return result;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.trace("hasPermission start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetId = {}, targetType = {}, permission = {}" , authentication, targetId, targetType, permission);

        Assert.notNull(authentication, "An authentication is required");
        Assert.notNull(targetType, "A targetType is required");
        Assert.notNull(permission, "A permission is required");

        boolean result = false;

        if (targetId != null && InstitutionResource.class.getSimpleName().equals(targetType)) {
            result = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(SelfCareGrantedAuthority.class::cast)
                    .filter(grantedAuthority -> targetId.toString().equals(grantedAuthority.getInstitutionId()))
                    .anyMatch(grantedAuthority -> ANY_PERMISSION.equals(permission) || permission.equals(grantedAuthority.getAuthority()));
        }

        log.debug("hasPermission result = {}", result);
        log.trace("hasPermission end");
        return result;
    }

}
