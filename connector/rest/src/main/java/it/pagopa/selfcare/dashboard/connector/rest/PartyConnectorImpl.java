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

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.PENDING;

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
    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> ACTIVE.name().equals(inst1.getStatus()) ? inst1 : inst2;
    private static final Function<OnboardingData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setInstitutionId(onboardingData.getInstitutionId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setStatus(onboardingData.getState().toString());
        if (onboardingData.getAttributes() != null && !onboardingData.getAttributes().isEmpty()) {
            institutionInfo.setCategory(onboardingData.getAttributes().get(0).getDescription());
        }
        return institutionInfo;
    };

    private final PartyProcessRestClient restClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public InstitutionInfo getInstitution(String institutionId) {
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));

        return parseOnBoardingInfo(onBoardingInfo).stream()
                .findAny().orElse(null);
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE, PENDING));

        return parseOnBoardingInfo(onBoardingInfo);
    }


    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo) {
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getInstitutionId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        return institutions;
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
    public Collection<AuthInfo> getAuthInfo(String institutionId) {
        Collection<AuthInfo> authInfos = Collections.emptyList();

        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            authInfos = onBoardingInfo.getInstitutions().stream()
                    .filter(onboardingData -> ACTIVE.equals(onboardingData.getState()))
                    .filter(onboardingData -> onboardingData.getProductInfo() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.groupingBy(OnboardingData::getInstitutionId,
                                    Collectors.mapping(onboardingData -> new ProductRole() {
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
                                    }, Collectors.toList())),
                            map -> map.entrySet().stream()
                                    .map(entry -> new AuthInfo() {
                                        @Override
                                        public String getInstitutionId() {
                                            return entry.getKey();
                                        }

                                        @Override
                                        public Collection<ProductRole> getProductRoles() {
                                            return Collections.unmodifiableCollection(entry.getValue());
                                        }
                                    }).collect(Collectors.toList())
                    ));
        }

        return authInfos;
    }

}
