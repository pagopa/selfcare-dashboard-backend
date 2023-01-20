package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.web.model.GeographicTaxonomyDto;
import it.pagopa.selfcare.dashboard.web.model.GeographicTaxonomyResource;

public class GeographicTaxonomyMapper {

    public static GeographicTaxonomyResource toResource(GeographicTaxonomy model) {
        GeographicTaxonomyResource resource = null;
        if (model != null) {
            resource = new GeographicTaxonomyResource();
            resource.setCode(model.getCode());
            resource.setDesc(model.getDesc());
        }
        return resource;
    }

    public static GeographicTaxonomy fromDto(GeographicTaxonomyDto resource) {
        GeographicTaxonomy model = null;
        if (resource != null) {
            model = new GeographicTaxonomy();
            model.setCode(resource.getCode());
            model.setDesc(resource.getDesc());
        }

        return model;
    }
}
