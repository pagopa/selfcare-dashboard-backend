package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfCarePermissionEvaluatorV2Test {

    @Mock
    UserApiConnector userApiConnector;
    @Mock
    UserGroupConnector userGroupConnector;
    @InjectMocks
    SelfCarePermissionEvaluatorV2 permissionEvaluator;

    @Test
    void hasPermissionReturnsTrueForValidUserGroupPermission() {
        Authentication authentication = mock(Authentication.class);
        SelfCareUser user = SelfCareUser.builder("userId").build();
        UserGroupInfo userGroupInfo = new UserGroupInfo();
        userGroupInfo.setInstitutionId("institutionId");
        userGroupInfo.setProductId("productId");

        when(authentication.getPrincipal()).thenReturn(user);
        when(userGroupConnector.getUserGroupById("groupId")).thenReturn(userGroupInfo);
        when(userApiConnector.hasPermission("userId", "institutionId", "productId", "Selc:ViewBilling")).thenReturn(true);

        assertTrue(permissionEvaluator.hasPermission(authentication, new FilterAuthorityDomain("institutionId", "productId", "groupId"), "Selc:ViewBilling"));
    }

    @Test
    void hasPermissionReturnsFalseForInvalidUserGroupPermission() {
        Authentication authentication = mock(Authentication.class);
        SelfCareUser user = SelfCareUser.builder("userId").build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(userApiConnector.hasPermission("userId", "institutionId", "productId", "Selc:ViewBilling")).thenReturn(true);

        assertTrue(permissionEvaluator.hasPermission(authentication, new FilterAuthorityDomain("institutionId", "productId", null), "Selc:ViewBilling"));
    }


    @Test
    void hasPermissionReturnsFalseForInvalidDirectPermission() {
        Authentication authentication = mock(Authentication.class);
        SelfCareUser user = SelfCareUser.builder("userId").build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(userApiConnector.hasPermission("userId", "institutionId", "productId", "Selc:ViewBilling")).thenReturn(false);

        assertFalse(permissionEvaluator.hasPermission(authentication, new FilterAuthorityDomain("institutionId", "productId", null), "Selc:ViewBilling"));
    }

    @Test
    void hasPermissionReturnsFalseWhenUserGroupNotFound() {
        Authentication authentication = mock(Authentication.class);
        SelfCareUser user = SelfCareUser.builder("userId").build();


        when(authentication.getPrincipal()).thenReturn(user);
        when(userGroupConnector.getUserGroupById("invalidGroupId")).thenReturn(null);

        assertFalse(permissionEvaluator.hasPermission(authentication, new FilterAuthorityDomain("institutionId", "productId", "invalidGroupId"), "Selc:ViewBilling"));
    }
}
