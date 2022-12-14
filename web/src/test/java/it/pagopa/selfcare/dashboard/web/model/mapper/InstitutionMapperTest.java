package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InstitutionMapperTest {

    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void toResourceNotNull() {
        // given
        String internalInstitutionId = "institutionId";
        String institutionId = "externalId";
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo(), "setId");
        institutionInfo.setId(internalInstitutionId);
        institutionInfo.setExternalId(institutionId);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(internalInstitutionId, Collections.singleton(new ProductGrantedAuthority(OPERATOR, "productRole", "productId")))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertEquals(institutionInfo.getExternalId(), resource.getExternalId());
        assertEquals(institutionInfo.getId(), resource.getId());
        assertEquals(institutionInfo.getInstitutionType(), resource.getInstitutionType());
        assertEquals(institutionInfo.getOriginId(), resource.getOriginId());
        assertEquals(institutionInfo.getOrigin(), resource.getOrigin());
        assertEquals(institutionInfo.getCategory(), resource.getCategory());
        assertEquals(institutionInfo.getDescription(), resource.getName());
        assertEquals(institutionInfo.getTaxCode(), resource.getFiscalCode());
        assertEquals(institutionInfo.getDigitalAddress(), resource.getMailAddress());
        assertEquals(institutionInfo.getAddress(), resource.getAddress());
        assertEquals(LIMITED.name(), resource.getUserRole());
        assertEquals(institutionInfo.getStatus().toString(), resource.getStatus());
        assertEquals(institutionInfo.getZipCode(), resource.getZipCode());
        reflectionEqualsByName(institutionInfo, resource, "status");
    }


    @Test
    void toResource_notNullNoAuth() {
        // given
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo());
        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertNull(resource.getUserRole());
    }


    @Test
    void toResource_emptyAuthNoPendingStatus() {
        // given
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo(), "setStatus");
        institutionInfo.setStatus(ACTIVE);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertNull(resource.getUserRole());
    }


    @Test
    void toResource_emptyAuthPendingStatus() {
        // given
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo(), "setStatus");
        institutionInfo.setStatus(PENDING);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertEquals(ADMIN.toString(), resource.getUserRole());
    }


    @Test
    void toResource_AuthOnDifferentInstitutionPendingStatus() {
        // given
        InstitutionInfo institutionInfo = mockInstance(new InstitutionInfo(), "setStatus");
        institutionInfo.setStatus(PENDING);
        String institutionId = "institutionId";
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(new ProductGrantedAuthority(OPERATOR, "productRole", "productId")))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertEquals(ADMIN.toString(), resource.getUserRole());
    }


    @Test
    void toResourceNull() {
        // given and when
        InstitutionResource institutionResource = InstitutionMapper.toResource(null);
        // then
        assertNull(institutionResource);
    }

}