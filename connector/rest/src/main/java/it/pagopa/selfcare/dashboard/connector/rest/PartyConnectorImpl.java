package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
class PartyConnectorImpl implements PartyConnector {

    private final PartyProcessRestClient restClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo getInstitutionInfo(String institutionId) {
        it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo institutionInfo = null;
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId);
        if (onBoardingInfo != null && !onBoardingInfo.getInstitutions().isEmpty()) {
            institutionInfo = new it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo() {
                @Override
                public String getInstitutionId() {
                    return onBoardingInfo.getInstitutions().get(0).getInstitutionId();
                }

                @Override
                public String getDescription() {
                    return onBoardingInfo.getInstitutions().get(0).getDescription();
                }

                @Override
                public String getDigitalAddress() {
                    return onBoardingInfo.getInstitutions().get(0).getDigitalAddress();
                }

                @Override
                public String getStatus() {
                    return onBoardingInfo.getInstitutions().get(0).getStatus();
                }

                @Override
                public String getRole() {
                    return onBoardingInfo.getInstitutions().get(0).getRole();
                }

                @Override
                public String getPlatformRole() {
                    return onBoardingInfo.getInstitutions().get(0).getPlatformRole();
                }
            };
        }
        return institutionInfo;
    }


    @Override
    public AuthInfo getAuthInfo(String institutionId) {
        AuthInfo authInfo = null;

        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId);
        if (onBoardingInfo != null && !onBoardingInfo.getInstitutions().isEmpty()) {
            InstitutionInfo institutionInfo = onBoardingInfo.getInstitutions().get(0);
            authInfo = new AuthInfo() {
                @Override
                public String getRole() {
                    return institutionInfo.getPlatformRole();
                }

                @Override
                public Collection<String> getProducts() {
                    return null;
                }
            };
        }

        return authInfo;
    }

}
