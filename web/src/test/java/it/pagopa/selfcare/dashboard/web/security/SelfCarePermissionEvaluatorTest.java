package it.pagopa.selfcare.dashboard.web.security;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.List;

class SelfCarePermissionEvaluatorTest {

    private SelfCarePermissionEvaluator permissionEvaluator = new SelfCarePermissionEvaluator();


    @Test
    void hasPermission_withObjectDomain() {
        // given
        Authentication authentication = null;
        Object targetDomainObject = null;
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_invalidInput() {
        // given
        Authentication authentication = null;
        Serializable targetId = null;
        String targetType = null;
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_nullAuth() {
        // given
        Authentication authentication = null;
        Serializable targetId = null;
        String targetType = "targetType";
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_nullTargetType() {
        // given
        Authentication authentication = new TestingAuthenticationToken("username", "password");
        Serializable targetId = null;
        String targetType = null;
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_unsupportedTargetType() {
        // given
        Authentication authentication = new TestingAuthenticationToken("username", "password");
        Serializable targetId = null;
        String targetType = "unsupported_resource";
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_notSelfCareAuthenticationDetails() {
        // given
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password");
        authentication.setDetails("details");
        Serializable targetId = null;
        String targetType = "InstitutionResource";
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_notPermitted() {
        // given
        String institutionId = "institutionId";
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password");
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        Serializable targetId = "notPermittedInstitutionId";
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeInstitutionResource_permitted() {
        // given
        String institutionId = "institutionId";
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password");
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        String targetType = InstitutionResource.class.getSimpleName();
        Object permission = null;
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, institutionId, targetType, permission);
        // then
        Assertions.assertTrue(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeProductsResource_notSelfCareGrantedAuthority() {
        // given
        Serializable targetId = null;
        String targetType = ProductsResource.class.getSimpleName();
        Object permission = null;
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", "authority");
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeProductsResource_notPermitted() {
        // given
        Serializable targetId = null;
        String targetType = ProductsResource.class.getSimpleName();
        Object permission = null;
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(SelfCareAuthority.ADMIN, "productRole", "productId"));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        // then
        Assertions.assertFalse(hasPermission);
    }


    @Test
    void hasPermission_withTargetId_targetTypeProductsResource_permitted() {
        // given
        String targetType = ProductsResource.class.getSimpleName();
        Object permission = null;
        String productId = "productId";
        List<ProductGrantedAuthority> roleOnProducts = List.of(new ProductGrantedAuthority(SelfCareAuthority.ADMIN, "productRole", productId));
        List<GrantedAuthority> authorities = List.of(new SelfCareGrantedAuthority(roleOnProducts));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("username", "password", authorities);
        // when
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, productId, targetType, permission);
        // then
        Assertions.assertTrue(hasPermission);
    }

}