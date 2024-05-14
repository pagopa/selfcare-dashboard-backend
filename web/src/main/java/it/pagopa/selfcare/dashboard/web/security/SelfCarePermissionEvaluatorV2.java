package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.io.Serializable;

@Slf4j
public class SelfCarePermissionEvaluatorV2 implements PermissionEvaluator {
    private final UserApiConnector userApiConnector;

    private final UserGroupConnector userGroupConnector;

    public SelfCarePermissionEvaluatorV2(UserApiConnector userApiConnector, UserGroupConnector userGroupConnector) {
        this.userApiConnector = userApiConnector;
        this.userGroupConnector = userGroupConnector;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.info("start check Permission");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetDomainObject = {}, permission = {}", authentication, targetDomainObject, permission);

        Assert.notNull(permission, "A permission type is required");
        boolean result = false;

        if (targetDomainObject != null && ProductAclDomain.class.equals(targetDomainObject.getClass())) {
            ProductAclDomain productAclDomain = (ProductAclDomain) targetDomainObject;
            Assert.notNull(productAclDomain.getInstitutionId(), "InstitutionId is required");
            result = userApiConnector.hasPermission(productAclDomain.getInstitutionId(), permission.toString(), productAclDomain.getProductId());
        }

        log.debug("check Permission result = {}", result);
        log.trace("check Permission end");
        return result;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.trace("check Permission start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetId = {}, targetType = {}, permission = {}", authentication, targetId, targetType, permission);

        Assert.notNull(permission, "A permission type is required");

        boolean result = false;

        if (targetId != null && InstitutionResource.class.getSimpleName().equals(targetType)) {
            Assert.notNull(targetId.toString(), "InstitutionId is required");
            result = userApiConnector.hasPermission(targetId.toString(), permission.toString(), null);
        }

        if (targetId != null && UserGroupResource.class.getSimpleName().equals(targetType)) {
            Assert.notNull(targetId.toString(), "UserGroupId is required");
            UserGroupInfo userGroupInfo = userGroupConnector.getUserGroupById(targetId.toString());
            result = userApiConnector.hasPermission(userGroupInfo.getInstitutionId(), permission.toString(), userGroupInfo.getProductId());
        }

        log.debug("check Permission result = {}", result);
        log.trace("check Permission end");
        return result;
    }

}
