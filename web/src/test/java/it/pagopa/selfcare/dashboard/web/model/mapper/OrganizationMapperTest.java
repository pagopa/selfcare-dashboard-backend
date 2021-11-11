package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizationMapperTest {

    @Test
    void toResourceNotNull() {
        // given
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());
        OnBoardingInfo onBoardingInfo = TestUtils.mockInstance(new OnBoardingInfo());
        onBoardingInfo.setInstitutions(List.of(institutionInfo));

        // when
        OrganizationResource resource = OrganizationMapper.toResource(onBoardingInfo);
        // then
        assertEquals(institutionInfo.getInstitutionId(), resource.getId());
        assertEquals(null, resource.getLogo());//TODO
        assertEquals(null, resource.getOrganizationType());//TODO
        assertEquals(institutionInfo.getDescription(), resource.getOrganizationName());
        assertEquals(null, resource.getFiscalCode());//TODO
        assertEquals(institutionInfo.getDigitalAddress(), resource.getMailAddress());
        assertEquals(null, resource.getIPACode());//TODO
        TestUtils.reflectionEqualsByName(onBoardingInfo, resource);
    }

    @Test
    void toResourceNull() {
        // given and when
        OrganizationResource organizationResource = OrganizationMapper.toResource(null);
        // then
        assertNull(organizationResource);
    }
}