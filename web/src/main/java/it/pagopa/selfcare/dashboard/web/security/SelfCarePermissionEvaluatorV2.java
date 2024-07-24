package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;

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
        String userId = ((SelfCareUser) authentication.getPrincipal()).getId();
        if (targetDomainObject instanceof FilterAuthorityDomain filterAuthorityDomain) {
            if (StringUtils.hasText(filterAuthorityDomain.getGroupId())) {
                UserGroupInfo userGroupInfo = userGroupConnector.getUserGroupById(filterAuthorityDomain.getGroupId());
                if (Objects.nonNull(userGroupInfo)) {
                    result = userApiConnector.hasPermission(userId, userGroupInfo.getInstitutionId(), userGroupInfo.getProductId(), permission.toString());
                }
            } else {
                result = userApiConnector.hasPermission(userId, filterAuthorityDomain.getInstitutionId(), filterAuthorityDomain.getProductId(), permission.toString());
            }
            log.debug("check Permission result = {}", result);
            log.trace("check Permission end");
        }
        return result;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }

}
