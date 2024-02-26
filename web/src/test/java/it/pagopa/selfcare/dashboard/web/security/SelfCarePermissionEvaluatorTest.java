package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SelfCarePermissionEvaluator.class})
class SelfCarePermissionEvaluatorTest {

    @MockBean
    MsCoreConnector msCoreConnector;

    @Autowired
    SelfCarePermissionEvaluator permissionEvaluator;


    @Test
    void hasPermission_withObjectDomain_nullAuth() {
        // given
        Authentication authentication = null;
        Object targetDomainObject = new Object();
        Object permission = new Object();
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An authentication is required", e.getMessage());
    }


    @Test
    void hasPermission_withObjectDomain_nullPermission() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Object targetDomainObject = new Object();
        Object permission = null;
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A permission is required", e.getMessage());
    }


    @Test
    void hasPermission_withObjectDomain_nullTargetDomainObject() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Object targetDomainObject = null;
        Object permission = new Object();
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withObjectDomain_unsupportedTargetDomainObject() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Object targetDomainObject = new Object();
        Object permission = new Object();
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
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
        Object permission = LIMITED.toString();
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
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
        Object permission = ADMIN.toString();
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
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
        Object permission = "ANY";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertTrue(hasPermission);
    }


    @Test
    void hasPermission_withObjectDomain_anyPermittedAsLimited() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Object targetDomainObject = new ProductAclDomain(institutionId, productId);
        Object permission = "ANY";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(OPERATOR, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        assertTrue(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_nullAuth() {
        // given
        Authentication authentication = null;
        Serializable targetId = "targetId";
        String targetType = "targetType";
        Object permission = new Object();
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An authentication is required", e.getMessage());
    }


    @Test
    void hasPermission_withTargetId_nullTargetType() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Serializable targetId = "targetId";
        String targetType = null;
        Object permission = new Object();
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A targetType is required", e.getMessage());
    }


    @Test
    void hasPermission_withTargetId_nullPermission() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Serializable targetId = "targetId";
        String targetType = "targetType";
        Object permission = null;
        // when
        Executable executable = () -> permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A permission is required", e.getMessage());
    }


    @Test
    void hasPermission_withTargetId_nullTargetId() {
        // given
        Authentication authentication = new TestingAuthenticationToken(null, null);
        Serializable targetId = "targetId";
        String targetType = "targetType";
        Object permission = new Object();
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_unsupportedTargetType() {
        // given
        Authentication authentication = new TestingAuthenticationToken("username", "password");
        Serializable targetId = "targetId";
        String targetType = "unsupported_resource";
        Object permission = new Object();
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_notPermitted_noAuthority() {
        // given
        Serializable targetId = "notPermittedInstitutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = new Object();
        String institutionId = "institutionId";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(institutionId, roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_notPermitted_invalidRole() {
        // given
        Serializable targetId = "institutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = LIMITED.toString();
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(targetId.toString(), roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_permitted() {
        // given
        Serializable targetId = "institutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = ADMIN.toString();
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(targetId.toString(), roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertTrue(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource__anyPermittedAsAdmin() {
        // given
        Serializable targetId = "institutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = "ANY";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(targetId.toString(), roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertTrue(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource__anyPermittedAsLimited() {
        // given
        Serializable targetId = "institutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = "ANY";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(OPERATOR, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(targetId.toString(), roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertTrue(hasPermission);
    }

    @Test
    void hasPermission_withTargetId_targetTypeRelationshipId_notPermitted_invalidRole() {
        // given
        Serializable targetId = "relationshipId";
        String targetType = "relationshipId";
        Object permission = LIMITED.toString();
        UserInfo userInfo = getUserInfo();
        when(msCoreConnector.getUser(anyString())).thenReturn(userInfo);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(targetId.toString(), roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeRelationshipId_permitted() {
        // given
        Serializable targetId = "relationshipId";
        String targetType = "relationshipId";
        Object permission = ADMIN.toString();
        UserInfo userInfo = getUserInfo();
        when(msCoreConnector.getUser(anyString())).thenReturn(userInfo);
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(MANAGER, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority("institutionId", roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        assertTrue(hasPermission);
    }

    private static UserInfo getUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setInstitutionId("institutionId");
        Map<String, ProductInfo> map = new HashMap<>();
        map.put("productId", new ProductInfo());
        userInfo.setProducts(map);
        return userInfo;
    }

}