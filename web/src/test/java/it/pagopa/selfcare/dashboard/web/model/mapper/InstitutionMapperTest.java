package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
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
        String institutionId = "institutionId";
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());
        SelfCareAuthority selcRole = LIMITED;
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(new ProductGrantedAuthority(selcRole, "productRole", "productId")))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertEquals(institutionInfo.getInstitutionId(), resource.getId());
        assertEquals(institutionInfo.getCategory(), resource.getCategory());
        assertEquals(institutionInfo.getDescription(), resource.getName());
        assertEquals(institutionInfo.getTaxCode(), resource.getFiscalCode());
        assertEquals(institutionInfo.getDigitalAddress(), resource.getMailAddress());
        assertEquals(selcRole.name(), resource.getUserRole());
        assertEquals(institutionInfo.getStatus(), resource.getStatus());
        TestUtils.reflectionEqualsByName(institutionInfo, resource);
    }


    @Test
    void toResourceNotNullNoAuth() {
        // given
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());
        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertNull(resource.getUserRole());
    }


    @Test
    void toResourceNull() {
        // given and when
        InstitutionResource institutionResource = InstitutionMapper.toResource(null);
        // then
        assertNull(institutionResource);
    }

}