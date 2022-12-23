package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.DataProtectionOfficer;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.PaymentServiceProvider;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.web.model.onboarding.OnboardingRequestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.commons.utils.TestUtils.reflectionEqualsByName;
import static org.junit.jupiter.api.Assertions.*;

class OnboardingRequestMapperTest {

    @Test
    void toOnboardingRequestResource_nullInput() {
        // given
        final OnboardingRequestInfo dto = null;
        // when
        final OnboardingRequestResource resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNull(resource);
    }


    @Test
    void toOnboardingRequestResource() {
        // given
        final OnboardingRequestInfo dto = mockInstance(new OnboardingRequestInfo());
        dto.getInstitutionInfo().setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        dto.getManager().getUser().setId(UUID.randomUUID().toString());
        dto.setAdmins(List.of(dto.getManager()));
        // when
        final OnboardingRequestResource resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNotNull(resource);
        assertNotNull(resource.getInstitutionInfo());
        assertNotNull(resource.getInstitutionInfo().getGeographicTaxonomies());
        assertNotNull(resource.getManager());
        assertNotNull(resource.getAdmins());
        assertEquals(dto.getInstitutionInfo().getStatus().toString(), resource.getStatus().toString());
    }


    @Test
    void toInstitutionInfoResource_nullInput() {
        // given
        final InstitutionInfo dto = null;
        // when
        final OnboardingRequestResource.InstitutionInfo resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNull(resource);
    }


    @Test
    void toInstitutionInfoResource() {
        // given
        final InstitutionInfo dto = mockInstance(new InstitutionInfo());
        dto.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        // when
        final OnboardingRequestResource.InstitutionInfo resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNotNull(resource);
        assertEquals(dto.getId(), resource.getId());
        assertEquals(dto.getInstitutionType(), resource.getInstitutionType());
        assertEquals(dto.getDescription(), resource.getName());
        assertEquals(dto.getTaxCode(), resource.getFiscalCode());
        assertEquals(dto.getDigitalAddress(), resource.getMailAddress());
        assertEquals(dto.getAddress(), resource.getAddress());
        assertEquals(dto.getZipCode(), resource.getZipCode());
        assertEquals(dto.getBilling().getRecipientCode(), resource.getRecipientCode());
        assertEquals(dto.getBilling().getVatNumber(), resource.getVatNumber());
        assertEquals(dto.getGeographicTaxonomies().get(0).getCode(), resource.getGeographicTaxonomies().get(0).getCode());
        assertEquals(dto.getGeographicTaxonomies().get(0).getDesc(), resource.getGeographicTaxonomies().get(0).getDesc());
        reflectionEqualsByName(dto.getPaymentServiceProvider(), resource.getPspData());
        reflectionEqualsByName(dto.getDataProtectionOfficer(), resource.getDpoData());
    }


    @Test
    void toPspDataResource_nullInput() {
        // given
        final PaymentServiceProvider dto = null;
        // when
        final OnboardingRequestResource.InstitutionInfo.PspData resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNull(resource);
    }


    @Test
    void toPspDataResource() {
        // given
        final PaymentServiceProvider dto = mockInstance(new PaymentServiceProvider());
        // when
        final OnboardingRequestResource.InstitutionInfo.PspData resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNotNull(resource);
        reflectionEqualsByName(dto, resource);
    }


    @Test
    void toDpoDataResource_nullInput() {
        // given
        final DataProtectionOfficer dto = null;
        // when
        final OnboardingRequestResource.InstitutionInfo.DpoData resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNull(resource);
    }


    @Test
    void toDpoDataResource() {
        // given
        final DataProtectionOfficer dto = mockInstance(new DataProtectionOfficer());
        // when
        final OnboardingRequestResource.InstitutionInfo.DpoData resource = OnboardingRequestMapper.toResource(dto);
        // then
        assertNotNull(resource);
        reflectionEqualsByName(dto, resource);
    }


    @Test
    void toUserInfoResource_nullInput() {
        // given
        final User dto = null;
        final String institutionId = null;
        // when
        final OnboardingRequestResource.UserInfo resource = OnboardingRequestMapper.toResource(dto, institutionId);
        // then
        assertNull(resource);
    }


    @Test
    void toUserInfoResource() {
        // given
        final String institutionId = "institutionId";
        final User dto = mockInstance(new User(), "setId");
        dto.setId(UUID.randomUUID().toString());
        dto.setWorkContacts(Map.of(institutionId, mockInstance(new WorkContact())));
        // when
        final OnboardingRequestResource.UserInfo resource = OnboardingRequestMapper.toResource(dto, institutionId);
        // then
        assertNotNull(resource);
        assertEquals(dto.getId(), resource.getId().toString());
        assertEquals(dto.getName().getValue(), resource.getName());
        assertEquals(dto.getFamilyName().getValue(), resource.getSurname());
        assertEquals(dto.getFiscalCode(), resource.getFiscalCode());
        assertEquals(dto.getWorkContacts().get(institutionId).getEmail().getValue(), resource.getEmail());
    }

}