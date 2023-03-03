package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.SupportContact;
import it.pagopa.selfcare.dashboard.web.model.SupportContactResource;

public class SupportContactMapper {

    public static SupportContactResource toResource(SupportContact model) {
        SupportContactResource resource = null;
        if (model != null) {
            resource = new SupportContactResource();
            resource.setSupportEmail(model.getSupportEmail());
            resource.setSupportPhone(model.getSupportPhone());
        }
        return resource;
    }

}
