package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;
import java.util.List;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SelfCarePermissionEvaluatorV2.class})
class SelfCarePermissionEvaluatorV2Test {

    @MockBean
    UserApiConnector userApiConnector;

    @MockBean
    UserGroupConnector userGroupConnector;

    @Autowired
    SelfCarePermissionEvaluatorV2 permissionEvaluator;

    @Test
    void hasPermission_withObjectDomain_nullPermission() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Object targetDomainObject = new Object();
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetDomainObject, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A permission type is required", e.getMessage());
    }


    @Test
    void hasPermission_withObjectDomain_nullTargetDomainObject() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Object permission = new Object();
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, null, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withObjectDomain_notPermitted_noAuthority() {
        // given
        String institutionId = "institutionId";
        Object targetDomainObject = new ProductAclDomain(institutionId, "notPermittedProductId");
        Object permission = new Object();
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertFalse(hasPermission);
    }

    @Test
    void hasPermission_withObjectDomain_notPermitted_invalidRole() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Object targetDomainObject = new ProductAclDomain(institutionId, productId);
        String permission = "ADMIN";
        when(userApiConnector.hasPermission(institutionId, permission, productId)).thenReturn(false);
        // when
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertFalse(hasPermission);
    }

    @Test
    void hasPermission_withObjectDomain_permitted() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Object targetDomainObject = new ProductAclDomain(institutionId, productId);
        String permission = "ADMIN";

        when(userApiConnector.hasPermission(institutionId, permission, productId)).thenReturn(true);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertTrue(hasPermission);
    }

    @Test
    void hasPermission_withObjectDomain_anyPermittedAsAdmin() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Object targetDomainObject = new ProductAclDomain(institutionId, productId);
        String permission = "ANY";
        when(userApiConnector.hasPermission(institutionId, permission, productId)).thenReturn(true);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertTrue(hasPermission);
    }

    @Test
    void hasPermission_withTargetId_nullPermission() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Serializable targetId = "targetId";
        String targetType = "targetType";
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetId, targetType, null);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A permission type is required", e.getMessage());
    }


    @Test
    void hasPermission_withTargetId_nullTargetId() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        String targetType = "targetType";
        Object permission = new Object();
        // when
        boolean result = permissionEvaluator.hasPermission(authentication, null, targetType, permission);
        // then
        assertFalse(result);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_notPermitted_noAuthority() {
        // given
        Serializable targetId = "notPermittedInstitutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        String permission = "ADMIN";
        String institutionId = "institutionId";
        when(userApiConnector.hasPermission(institutionId, permission, null)).thenReturn(false);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);

        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }

    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_permitted() {
        // given
        Serializable targetId = "institutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        String  permission = ADMIN.toString();
        when(userApiConnector.hasPermission(targetId.toString(), permission, null)).thenReturn(true);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertTrue(hasPermission);
    }

    @Test
    void hasPermission_withTargetId_targetTypeUserGroupResource_permitted() {
        // given
        Serializable targetId = "groupId";
        String targetType = UserGroupResource.class.getSimpleName();
        String  permission = ADMIN.toString();
        UserGroupInfo userGroupResource = new UserGroupInfo();
        userGroupResource.setId(targetId.toString());
        userGroupResource.setInstitutionId("institutionId");
        userGroupResource.setProductId("productId");
        when(userGroupConnector.getUserGroupById(targetId.toString())).thenReturn(userGroupResource);
        when(userApiConnector.hasPermission(userGroupResource.getInstitutionId(), permission, userGroupResource.getProductId())).thenReturn(true);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertTrue(hasPermission);
    }

    @Test
    void hasPermission_withTargetId_targetTypeUserGroupResource_not_permitted() {
        // given
        Serializable targetId = "groupId";
        String targetType = UserGroupResource.class.getSimpleName();
        String  permission = ADMIN.toString();
        UserGroupInfo userGroupResource = new UserGroupInfo();
        userGroupResource.setId(targetId.toString());
        userGroupResource.setInstitutionId("institutionId");
        userGroupResource.setProductId("productId");
        when(userGroupConnector.getUserGroupById(targetId.toString())).thenReturn(userGroupResource);
        when(userApiConnector.hasPermission(userGroupResource.getInstitutionId(), permission, userGroupResource.getProductId())).thenReturn(false);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", permission);
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }

}