package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InstitutionMapperTest {

    @Test
    void toResourceNotNull() {
        // given
        InstitutionInfo institutionInfo = TestUtils.mockInstance(new InstitutionInfo());

        // when
        InstitutionResource resource = InstitutionMapper.toResource(institutionInfo);
        // then
        assertEquals(institutionInfo.getInstitutionId(), resource.getId());
        assertEquals(null, resource.getCategory());//TODO
        assertEquals(institutionInfo.getDescription(), resource.getName());
        assertEquals(null, resource.getFiscalCode());//TODO
        assertEquals(institutionInfo.getDigitalAddress(), resource.getMailAddress());
        assertEquals(null, resource.getIPACode());//TODO
        TestUtils.reflectionEqualsByName(institutionInfo, resource);
    }


    @Test
    void toResourceNull() {
        // given and when
        InstitutionResource institutionResource = InstitutionMapper.toResource(null);
        // then
        assertNull(institutionResource);
    }

}