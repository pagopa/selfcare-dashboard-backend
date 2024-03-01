package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.io.Serializable;

@Slf4j
public class SelfCarePermissionEvaluatorV2 implements PermissionEvaluator {
    private final UserApiConnector userApiConnector;

    public SelfCarePermissionEvaluatorV2(UserApiConnector userApiConnector) {
        this.userApiConnector = userApiConnector;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.trace("hasPermission start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetDomainObject = {}, permission = {}", authentication, targetDomainObject, permission);

        Assert.notNull(authentication, "An authentication is required");
        Assert.notNull(permission, "A permission is required");
        boolean result = false;
        if (targetDomainObject != null && ProductAclDomain.class.equals(targetDomainObject.getClass())) {
            ProductAclDomain productAclDomain = (ProductAclDomain) targetDomainObject;
            Assert.notNull(productAclDomain.getInstitutionId(), "InstitutionId is required");
            result = userApiConnector.hasPermission(productAclDomain.getInstitutionId(), permission.toString(), productAclDomain.getProductId());
        }
        log.debug("hasPermission result = {}", result);
        log.trace("hasPermission end");
        return result;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.trace("hasPermission start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetId = {}, targetType = {}, permission = {}", authentication, targetId, targetType, permission);

        Assert.notNull(authentication, "An authentication is required");
        Assert.notNull(targetType, "A targetType is required");
        Assert.notNull(permission, "A permission is required");

        boolean result = false;

        if (targetId != null && InstitutionResource.class.getSimpleName().equals(targetType)) {
            Assert.notNull(targetId.toString(), "InstitutionId is required");
            result = userApiConnector.hasPermission(targetId.toString(), permission.toString(), null);
        }
        log.debug("hasPermission result = {}", result);
        log.trace("hasPermission end");
        return result;
    }

}
