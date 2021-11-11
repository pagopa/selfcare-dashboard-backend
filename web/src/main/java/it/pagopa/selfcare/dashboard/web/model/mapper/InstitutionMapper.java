package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;

public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;

        if (model != null) {
            resource = new InstitutionResource();
            resource.setId(model.getInstitutionId());
            resource.setName(model.getDescription());
            resource.setType(null);//TODO
            resource.setIPACode(null);//TODO
            resource.setFiscalCode(null);//TODO
            resource.setMailAddress(model.getDigitalAddress());
        }

        return resource;
    }
}
