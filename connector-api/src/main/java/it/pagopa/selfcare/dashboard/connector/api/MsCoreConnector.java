package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;

public interface MsCoreConnector {

    InstitutionInfo getPGInstitutionByExternalId(String externalId);
}
