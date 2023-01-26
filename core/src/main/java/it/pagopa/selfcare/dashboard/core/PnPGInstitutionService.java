package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;

public interface PnPGInstitutionService {

    InstitutionInfo getPGInstitutionByExternalId(String externalId);

}
