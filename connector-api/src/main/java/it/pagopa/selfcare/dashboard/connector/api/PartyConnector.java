package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;

public interface PartyConnector {

    InstitutionInfo getInstitutionInfo(String institutionId);

    AuthInfo getAuthInfo(String institutionId);

}
