package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.UserService;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;

@Slf4j
public class SelfCarePermissionEvaluator implements PermissionEvaluator {

    public static final String ANY_PERMISSION = "ANY";
    public static final String RELATIONSHIP_ID = "relationshipId";
    private final MsCoreConnector msCoreConnector;

    public SelfCarePermissionEvaluator(MsCoreConnector msCoreConnector) {
        this.msCoreConnector = msCoreConnector;
    }


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

        if (targetId != null && RELATIONSHIP_ID.equals(targetType)) {
            UserInfo userInfo = msCoreConnector.getUser(targetId.toString());
            if(!CollectionUtils.isEmpty(userInfo.getProducts()) && userInfo.getProducts().size() == 1) {
                String productId = userInfo.getProducts().keySet().stream().toList().get(0);
                result = authentication.getAuthorities()
                        .stream()
                        .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                        .map(SelfCareGrantedAuthority.class::cast)
                        .filter(grantedAuthority -> grantedAuthority.getInstitutionId().equals(userInfo.getInstitutionId()))
                        .filter(grantedAuthority -> grantedAuthority.getRoleOnProducts().containsKey(productId))
                        .map(grantedAuthority -> grantedAuthority.getRoleOnProducts().get(productId))
                        .anyMatch(grantedAuthority -> ANY_PERMISSION.equals(permission) || permission.equals(grantedAuthority.getAuthority()));
            }
        }

        log.debug("hasPermission result = {}", result);
        log.trace("hasPermission end");
        return result;
    }

}
