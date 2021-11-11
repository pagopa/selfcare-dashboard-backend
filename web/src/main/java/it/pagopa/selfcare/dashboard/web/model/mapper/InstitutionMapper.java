package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.onboarding.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;

public class InstitutionMapper {

    public static InstitutionResource toResource(OnBoardingInfo model) {
        InstitutionResource resource;

        try {
            InstitutionInfo institutionInfo = model.getInstitutions().get(0);
            resource = new InstitutionResource();
            resource.setId(institutionInfo.getInstitutionId());
            resource.setName(institutionInfo.getDescription());
            resource.setType(null);//TODO
            resource.setIPACode(null);//TODO
            resource.setFiscalCode(null);//TODO
            resource.setMailAddress(institutionInfo.getDigitalAddress());

        } catch (Exception e) {
            resource = null;
        }

        return resource;
    }
}
