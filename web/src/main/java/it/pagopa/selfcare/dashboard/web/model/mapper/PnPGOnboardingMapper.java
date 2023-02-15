package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.dashboard.web.model.PnPGInstitutionLegalAddressResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PnPGOnboardingMapper {

    public static PnPGInstitutionLegalAddressResource toResource(PnPGInstitutionLegalAddressData model) {
        PnPGInstitutionLegalAddressResource resource = null;
        if (model != null) {
            resource = new PnPGInstitutionLegalAddressResource();

            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
        }
        return resource;
    }

}
