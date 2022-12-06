package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.web.model.GeographicTaxonomyResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GeographicTaxonomyMapperTest {


    @Test
    void toGeographicTaxonomyResource_notNull(){
        // given
        GeographicTaxonomy model = new GeographicTaxonomy();
        // when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toGeographicTaxonomyResource(model);
        // then
        assertEquals(model.getCode(), resource.getCode());
        assertEquals(model.getDesc(), resource.getDesc());
    }

    @Test
    void toGeographicTaxonomyResource_null(){
        // given
        GeographicTaxonomy model = null;
        // when
        GeographicTaxonomyResource resource = GeographicTaxonomyMapper.toGeographicTaxonomyResource(model);
        // then
        assertNull(resource);
    }


}
