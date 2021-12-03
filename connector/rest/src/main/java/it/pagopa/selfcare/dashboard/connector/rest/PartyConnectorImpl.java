package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class PartyConnectorImpl implements PartyConnector {

    static final Function<PartyRole, SelfCareAuthority> PARTY_2_SELC_ROLE = partyRole -> {
        SelfCareAuthority selfCareRole;
        switch (partyRole) {
            case MANAGER:
            case DELEGATE:
            case SUB_DELEGATE:
                selfCareRole = SelfCareAuthority.ADMIN;
                break;
            default:
                selfCareRole = SelfCareAuthority.LIMITED;
        }
        return selfCareRole;
    };

    private final PartyProcessRestClient restClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public InstitutionInfo getInstitutionInfo(String institutionId) {
        InstitutionInfo institutionInfo = null;
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId);

        if (onBoardingInfo != null
                && onBoardingInfo.getInstitutions() != null
                && !onBoardingInfo.getInstitutions().isEmpty()) {
            OnboardingData onboardingData = onBoardingInfo.getInstitutions().get(0);
            institutionInfo = new InstitutionInfo();
            institutionInfo.setInstitutionId(onboardingData.getInstitutionId());
            institutionInfo.setDescription(onboardingData.getDescription());
            institutionInfo.setTaxCode(onboardingData.getTaxCode());
            institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
            institutionInfo.setStatus(onboardingData.getState().toString());
            if (onboardingData.getAttributes() != null && !onboardingData.getAttributes().isEmpty()) {
                institutionInfo.setCategory(onboardingData.getAttributes().get(0).getDescription());
            }
        }

        return institutionInfo;
    }


    @Override
    public List<String> getInstitutionProducts(String institutionId) {//TODO: return also activationDate
        List<String> products = Collections.emptyList();
        Products institutionProducts = restClient.getInstitutionProducts(institutionId);
        if (institutionProducts != null && institutionProducts.getProducts() != null) {
            products = institutionProducts.getProducts().stream()
                    .map(ProductInfo::getId)
                    .collect(Collectors.toList());
        }

        return products;
    }


    @Override
    public AuthInfo getAuthInfo(String institutionId) {
        AuthInfo authInfo = null;

        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId);
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            authInfo = new AuthInfo() {
                @Override
                public Collection<ProductRole> getProductRoles() {
                    return onBoardingInfo.getInstitutions().stream()
                            .filter(onboardingData -> RelationshipState.ACTIVE.equals(onboardingData.getState()))
                            .filter(onboardingData -> onboardingData.getProductInfo() != null)
                            .map(onboardingData -> new ProductRole() {
                                @Override
                                public SelfCareAuthority getSelfCareRole() {
                                    return PARTY_2_SELC_ROLE.apply(onboardingData.getRole());
                                }

                                @Override
                                public String getProductRole() {
                                    return onboardingData.getProductInfo().getRole();
                                }

                                @Override
                                public String getProductId() {
                                    return onboardingData.getProductInfo().getId();
                                }
                            }).collect(Collectors.toList());
                }
            };
        }

        return authInfo;
    }

}
