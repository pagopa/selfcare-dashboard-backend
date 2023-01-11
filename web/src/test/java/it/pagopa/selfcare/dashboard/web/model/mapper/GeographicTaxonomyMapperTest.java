package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.web.model.GeographicTaxonomyDto;
import it.pagopa.selfcare.dashboard.web.model.GeographicTaxonomyResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeographicTaxonomyMapperTest {


    @Test
    void toGeographicTaxonomyResource_notNull(){
        // given
        GeographicTaxonomy model = new GeographicTaxonomy();
        // when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toResource(model);
        // then
        assertEquals(model.getCode(), resource.getCode());
        assertEquals(model.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomyResource_null() {
        // given
        GeographicTaxonomy model = null;
        // when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toGeographicTaxonomy_notNull() {
        // given
        GeographicTaxonomyDto dtoModel = new GeographicTaxonomyDto();
        // when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.fromDto(dtoModel);
        // then
        assertEquals(dtoModel.getCode(), resource.getCode());
        assertEquals(dtoModel.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomy_null() {
        // given
        GeographicTaxonomyDto dtoModel = null;
        // when
        GeographicTaxonomy resource = GeographicTaxonomyMapper.fromDto(dtoModel);
        // then
        assertNull(resource);
    }


}
