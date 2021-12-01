package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;

import java.util.List;

public interface PartyConnector {

    InstitutionInfo getInstitutionInfo(String institutionId);

    List<String> getInstitutionProducts(String institutionId);

    AuthInfo getAuthInfo(String institutionId);

}
