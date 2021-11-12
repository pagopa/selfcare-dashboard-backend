package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
class PartyConnectorImpl implements PartyConnector {

    private final PartyProcessRestClient restClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public InstitutionInfo getInstitutionInfo(String institutionId) {
        InstitutionInfo institutionInfo = null;
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId);
        if (onBoardingInfo != null && !onBoardingInfo.getInstitutions().isEmpty()) {
            OnboardingData onboardingData = onBoardingInfo.getInstitutions().get(0);
            institutionInfo = new InstitutionInfo() {
                @Override
                public String getInstitutionId() {
                    return onboardingData.getInstitutionId();
                }

                @Override
                public String getDescription() {
                    return onboardingData.getDescription();
                }

                @Override
                public String getDigitalAddress() {
                    return onboardingData.getDigitalAddress();
                }

                @Override
                public String getStatus() {
                    return onboardingData.getState().toString();
                }

                @Override
                public List<String> getActiveProducts() {
                    return onboardingData.getInstitutionProducts();
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
            OnboardingData onboardingData = onBoardingInfo.getInstitutions().get(0);
            authInfo = new AuthInfo() {
                @Override
                public String getRole() {
                    return onboardingData.getProductRole();
                }

                @Override
                public Collection<String> getProducts() {
                    return onboardingData.getRelationshipProducts();
                }
            };
        }

        return authInfo;
    }

}
