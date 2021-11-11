package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.organization.Organization;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;

public class OrganizationMapper {

    public static OrganizationResource toResource(Organization entity) {

        OrganizationResource resource = null;
        if (entity != null) {
            resource = new OrganizationResource();
            resource.setId(entity.getInstitutionId());
            resource.setLogo(null);//TODO
            resource.setOrganizationName(entity.getDescription());
            resource.setOrganizationType(null);//TODO
            resource.setIPACode(null);//TODO
            resource.setFiscalCode(null);//TODO
            resource.setMailAddress(entity.getDigitalAddress());
        }
        return resource;
    }
}
