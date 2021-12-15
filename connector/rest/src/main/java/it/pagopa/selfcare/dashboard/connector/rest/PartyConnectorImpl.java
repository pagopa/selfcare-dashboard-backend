package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.dashboard.connector.rest.model.PartyRole.*;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.PENDING;

@Service
class PartyConnectorImpl implements PartyConnector {

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
    static final EnumMap<PartyRole, SelfCareAuthority> PARTY_ROLE_AUTHORITY_MAP = new EnumMap<>(PartyRole.class);

    static {
        PARTY_ROLE_AUTHORITY_MAP.put(MANAGER, ADMIN);
        PARTY_ROLE_AUTHORITY_MAP.put(DELEGATE, ADMIN);
        PARTY_ROLE_AUTHORITY_MAP.put(SUB_DELEGATE, ADMIN);
        PARTY_ROLE_AUTHORITY_MAP.put(OPERATOR, LIMITED);
    }

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
                    .filter(onboardingData -> onboardingData.getProductInfo() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.groupingBy(OnboardingData::getInstitutionId,
                                    Collectors.mapping(onboardingData -> new ProductRole() {
                                        @Override
                                        public SelfCareAuthority getSelfCareRole() {
                                            return PARTY_ROLE_AUTHORITY_MAP.get(onboardingData.getRole());
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


    @Override
    public Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role, Optional<Set<String>> productIds) {
        Assert.hasText(institutionId, "An Institution id is required");
        Assert.notNull(role, "An Optional role object is required");
        Assert.notNull(productIds, "An Optional list of Product id object is required");
        Collection<UserInfo> userInfos = Collections.emptyList();
        EnumSet<PartyRole> roles = null;
        if (role.isPresent()) {
            roles = PARTY_ROLE_AUTHORITY_MAP.entrySet().stream()
                    .filter(entry -> role.get().equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        }
        RelationshipsResponse institutionRelationships = restClient.getInstitutionRelationships(institutionId, roles, null, productIds.orElse(null));
        if (institutionRelationships != null) {
            userInfos = institutionRelationships.stream()
                    .collect(Collectors.toMap(RelationshipInfo::getFrom,
                            relationshipInfo -> {
                                UserInfo userInfo = new UserInfo();
                                userInfo.setRelationshipId(relationshipInfo.getId());
                                userInfo.setId(relationshipInfo.getFrom());
                                userInfo.setName(relationshipInfo.getName());
                                userInfo.setSurname(relationshipInfo.getSurname());
                                userInfo.setEmail(relationshipInfo.getEmail());
                                userInfo.setStatus(relationshipInfo.getState().toString());
                                userInfo.setRole(PARTY_ROLE_AUTHORITY_MAP.get(relationshipInfo.getRole()));
                                it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo
                                        = new it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo();
                                productInfo.setId(relationshipInfo.getProduct().getId());
                                ArrayList<it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo> products = new ArrayList<>();
                                products.add(productInfo);
                                userInfo.setProducts(products);
                                return userInfo;
                            },
                            (userInfo1, userInfo2) -> {
                                userInfo1.getProducts().addAll(userInfo2.getProducts());
                                return userInfo1;
                            })).values();
        }

        return userInfos;
    }

}
