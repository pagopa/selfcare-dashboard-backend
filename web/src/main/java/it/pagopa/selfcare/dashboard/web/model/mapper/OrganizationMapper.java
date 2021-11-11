package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.onboarding.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;

public class OrganizationMapper {

    public static OrganizationResource toResource(OnBoardingInfo model) {
        OrganizationResource resource;

        try {
            InstitutionInfo institutionInfo = model.getInstitutions().get(0);
            resource = new OrganizationResource();
            resource.setId(institutionInfo.getInstitutionId());
            resource.setLogo(null);//TODO
            resource.setOrganizationName(institutionInfo.getDescription());
            resource.setOrganizationType(null);//TODO
            resource.setIPACode(null);//TODO
            resource.setFiscalCode(null);//TODO
            resource.setMailAddress(institutionInfo.getDigitalAddress());

        } catch (Exception e) {
            resource = null;
        }

        return resource;
    }
}
