package it.pagopa.selfcare.dashboard.security;

import io.github.resilience4j.retry.annotation.Retry;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.client.UserGroupRestClient;
import it.pagopa.selfcare.dashboard.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.model.groups.UserGroupStatus;
import it.pagopa.selfcare.dashboard.model.user.User;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import it.pagopa.selfcare.group.generated.openapi.v1.dto.UserGroupResource;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionWithActions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
public class SelfCarePermissionEvaluatorV2 implements PermissionEvaluator {

    private final UserGroupRestClient userGroupRestClient;
    private final UserApiRestClient userApiRestClient;
    static final String REQUIRED_GROUP_ID_MESSAGE = "A user group id is required";
    private static final String ISSUER_PAGOPA = "PAGOPA";
    private static final List<String> PAGOPA_ALLOWED_PERMISSIONS = List.of(
            "Selc:ViewInstitutionData",
            "Selc:AccessProductBackofficeAdmin"
    );


    public SelfCarePermissionEvaluatorV2(UserGroupRestClient restClient, UserApiRestClient userApiRestClient) {
        this.userGroupRestClient = restClient;
        this.userApiRestClient = userApiRestClient;
    }

    static final Function<UserGroupResource, UserGroupInfo> GROUP_RESPONSE_TO_GROUP_INFO = groupResponse -> {
        UserGroupInfo groupInfo = new UserGroupInfo();
        groupInfo.setId(groupResponse.getId());
        groupInfo.setInstitutionId(groupResponse.getInstitutionId());
        groupInfo.setProductId(groupResponse.getProductId());
        groupInfo.setName(groupResponse.getName());
        groupInfo.setDescription(groupResponse.getDescription());
        if (Objects.nonNull(groupResponse.getStatus())) {
            groupInfo.setStatus(UserGroupStatus.valueOf(groupResponse.getStatus().getValue()));
        }
        if (groupResponse.getMembers() != null) {
            List<UserInfo> members = groupResponse.getMembers().stream().map(id -> {
                UserInfo member = new UserInfo();
                member.setId(id.toString());
                return member;
            }).toList();
            groupInfo.setMembers(members);
        }
        groupInfo.setCreatedAt(groupResponse.getCreatedAt());
        groupInfo.setModifiedAt(groupResponse.getModifiedAt());
        User createdBy = new User();
        createdBy.setId(groupResponse.getCreatedBy());
        groupInfo.setCreatedBy(createdBy);
        if (groupResponse.getModifiedBy() != null) {
            User userInfo = new User();
            userInfo.setId(groupResponse.getModifiedBy());
            groupInfo.setModifiedBy(userInfo);
        }
        return groupInfo;
    };

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.info("start check Permission");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "hasPermission authentication = {}, targetDomainObject = {}, permission = {}", authentication, targetDomainObject, permission);
        Assert.notNull(permission, "A permission type is required");
        boolean result = false;

        SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        String userId = selfCareUser.getId();
        String issuer = selfCareUser.getIssuer();

        if (ISSUER_PAGOPA.equalsIgnoreCase(issuer)) {
            log.debug("Issuer is PAGOPA, evaluating permission {}", permission);

            boolean isAllowed = PAGOPA_ALLOWED_PERMISSIONS.stream()
                    .anyMatch(p -> p.equalsIgnoreCase(permission.toString()));

            log.debug("PAGOPA permission {} → {}", permission, isAllowed ? "GRANTED" : "DENIED");
            log.trace("check Permission end (issuer PAGOPA)");
            return isAllowed;
        }

        if (targetDomainObject instanceof FilterAuthorityDomain filterAuthorityDomain) {
            if (StringUtils.hasText(filterAuthorityDomain.getGroupId())) {
                UserGroupInfo userGroupInfo = getUserGroupById(filterAuthorityDomain.getGroupId());
                if (Objects.nonNull(userGroupInfo)) {
                    result = hasPermission1(userId, userGroupInfo.getInstitutionId(), userGroupInfo.getProductId(), permission.toString());
                }
            } else {
                result = hasPermission1(userId, filterAuthorityDomain.getInstitutionId(), filterAuthorityDomain.getProductId(), permission.toString());
            }
            log.debug("check Permission result = {}", result);
            log.trace("check Permission end");
        }
        return result;
    }

    private UserGroupInfo getUserGroupById(String id) {
        log.trace("getUserGroupById start");
        log.debug("getUseGroupById id = {}", id);
        Assert.hasText(id, REQUIRED_GROUP_ID_MESSAGE);
        UserGroupResource response = userGroupRestClient._getUserGroupUsingGET(id).getBody();
        UserGroupInfo groupInfo = GROUP_RESPONSE_TO_GROUP_INFO.apply(response);
        log.debug("getUseGroupById groupInfo = {}", groupInfo);
        log.trace("getUserGroupById end");
        return groupInfo;
    }

    @Retry(name = "retryTimeout")
    private Boolean hasPermission1(String userId, String institutionId, String productId, String action) {
        log.trace("permissionInstitutionIdPermissionGet start");
        log.debug("permissionInstitutionIdPermissionGet userId = {}, institutionId = {}, productId = {} for action = {}", userId, institutionId, productId, action);
        boolean result = false;
        UserInstitutionWithActions userInstitutionWithActions = userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, productId).getBody();
        if (Objects.nonNull(userInstitutionWithActions) && !CollectionUtils.isEmpty(userInstitutionWithActions.getProducts())) {
            result = userInstitutionWithActions.getProducts().stream().anyMatch(onboardedProductWithActions -> onboardedProductWithActions.getUserProductActions().contains(action));
        }
        log.debug("permissionInstitutionIdPermissionGet result = {}", result);
        log.trace("permissionInstitutionIdPermissionGet end");
        return result;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }

}
