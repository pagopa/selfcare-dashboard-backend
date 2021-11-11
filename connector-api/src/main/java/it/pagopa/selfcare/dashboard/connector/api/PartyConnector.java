package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.RelationshipsResponse;

public interface PartyConnector {

    RelationshipsResponse getInstitutionRelationships(String institutionId);

    OnBoardingInfo getOnBoardingInfo(String institutionId);

}
