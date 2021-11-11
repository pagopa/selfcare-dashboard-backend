package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.organization.Organization;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizationMapperTest {

    @Test
    void toResourceNotNull() {
        // given
        Organization organization = TestUtils.mockInstance(new Organization());
        // when
        OrganizationResource resource = OrganizationMapper.toResource(organization);
        // then
        assertEquals(organization.getInstitutionId(), resource.getId());
        assertEquals(null, resource.getLogo());//TODO
        assertEquals(null, resource.getOrganizationType());//TODO
        assertEquals(organization.getDescription(), resource.getOrganizationName());
        assertEquals(null, resource.getFiscalCode());//TODO
        assertEquals(organization.getDigitalAddress(), resource.getMailAddress());
        assertEquals(null, resource.getIPACode());//TODO
        TestUtils.reflectionEqualsByName(organization, resource);
    }

    @Test
    void toResourceNull() {
        // given and when
        OrganizationResource organizationResource = OrganizationMapper.toResource(null);
        // then
        assertNull(organizationResource);
    }
}