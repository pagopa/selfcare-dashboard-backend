package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Service
class PartyConnectorImpl implements PartyConnector {

    static final EnumMap<PartyRole, SelfCareAuthority> PARTY_ROLE_AUTHORITY_MAP = new EnumMap<>(PartyRole.class);
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
    private static final Function<RelationshipInfo, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
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
    };
    private static final Function<Product, PartyProduct> PRODUCT_INFO_TO_PRODUCT_FUNCTION = productInfo -> {
        PartyProduct product = new PartyProduct();
        product.setId(productInfo.getId());
        product.setStatus(ProductStatus.valueOf(productInfo.getState().toString()));
        return product;
    };

    static {
        PARTY_ROLE_AUTHORITY_MAP.put(MANAGER, ADMIN);
        PARTY_ROLE_AUTHORITY_MAP.put(DELEGATE, ADMIN);
        PARTY_ROLE_AUTHORITY_MAP.put(SUB_DELEGATE, ADMIN);
        PARTY_ROLE_AUTHORITY_MAP.put(OPERATOR, LIMITED);
    }

    private final PartyProcessRestClient restClient;
    private final EnumSet<RelationshipState> allowedStates;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient,
                              @Value("${dashboard.partyConnector.getUsers.filter.states}") String[] allowedStates) {
        this.restClient = restClient;
        this.allowedStates = allowedStates == null || allowedStates.length == 0
                ? null
                : EnumSet.copyOf(Arrays.stream(allowedStates)
                .map(RelationshipState::valueOf)
                .collect(Collectors.toList()));
    }


    @Override
    public InstitutionInfo getInstitution(String institutionId) {
        log.trace("PartyConnectorImpl.getInstitution start");
        log.debug("institutionId = {}", institutionId);
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        InstitutionInfo result = parseOnBoardingInfo(onBoardingInfo).stream()
                .findAny().orElse(null);
        log.debug("result = {}", result);
        log.trace("PartyConnectorImpl.getInstitution end");
        return result;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("PartyConnectorImpl.getInstitutions start");
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE, PENDING));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo);
        log.debug("result = {}", result);
        log.trace("PartyConnectorImpl.getInstitutions end");
        return result;
    }


    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo) {
        log.trace("PartyConnectorImpl.parseOnBoardingInfo start");
        log.debug("onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getInstitutionId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug("result = {}", institutions);
        log.trace("PartyConnectorImpl.parseOnBoardingInfo end");
        return institutions;
    }


    @Override
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("PartyConnectorImpl.getInstitutionProducts start");
        log.debug("institutionId = {}", institutionId);
        List<PartyProduct> products = Collections.emptyList();
        Products institutionProducts = restClient.getInstitutionProducts(institutionId, EnumSet.of(ProductState.ACTIVE, ProductState.PENDING));
        if (institutionProducts != null && institutionProducts.getProducts() != null) {
            products = institutionProducts.getProducts().stream()
                    .map(PRODUCT_INFO_TO_PRODUCT_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("result = {}", products);
        log.trace("PartyConnectorImpl.getInstitutionProducts end");
        return products;
    }


    @Override
    public Collection<AuthInfo> getAuthInfo(String institutionId) {
        log.trace("PartyConnectorImpl.getAuthInfo start");
        log.debug("institutionId = {}", institutionId);
        Collection<AuthInfo> authInfos = Collections.emptyList();
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            authInfos = onBoardingInfo.getInstitutions().stream()
                    .filter(onboardingData -> onboardingData.getProductInfo() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.groupingBy(OnboardingData::getInstitutionId,
                                    Collectors.mapping(onboardingData -> {
                                        PartyProductRole productRole = new PartyProductRole();
                                        productRole.setProductId(onboardingData.getProductInfo().getId());
                                        productRole.setProductRole(onboardingData.getProductInfo().getRole());
                                        productRole.setSelfCareRole(PARTY_ROLE_AUTHORITY_MAP.get(onboardingData.getRole()));
                                        return productRole;
                                    }, Collectors.toList())),
                            map -> map.entrySet().stream()
                                    .map(entry -> {
                                        PartyAuthInfo authInfo = new PartyAuthInfo();
                                        authInfo.setInstitutionId(entry.getKey());
                                        authInfo.setProductRoles(Collections.unmodifiableCollection(entry.getValue()));
                                        return authInfo;
                                    }).collect(Collectors.toList())
                    ));
        }
        log.debug("result = {}", authInfos);
        log.trace("PartyConnectorImpl.getAuthInfo end");
        return authInfos;
    }


    @Override
    public Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role, Optional<String> productId) {
        log.trace("PartyConnectorImpl.getUsers start");
        log.debug("institutionId = {}, role = {}, productId = {}", institutionId, role, productId);
        Assert.hasText(institutionId, "An Institution id is required");
        Assert.notNull(role, "An Optional role object is required");
        Assert.notNull(productId, "An Optional Product id object is required");
        Collection<UserInfo> userInfos = Collections.emptyList();
        EnumSet<PartyRole> roles = null;
        if (role.isPresent()) {
            roles = PARTY_ROLE_AUTHORITY_MAP.entrySet().stream()
                    .filter(entry -> role.get().equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        }
        RelationshipsResponse institutionRelationships = restClient.getInstitutionRelationships(institutionId, roles, allowedStates, productId.map(Set::of).orElse(null));
        if (institutionRelationships != null) {
            userInfos = institutionRelationships.stream()
                    .collect(Collectors.toMap(RelationshipInfo::getFrom,
                            RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION,
                            (userInfo1, userInfo2) -> {
                                userInfo1.getProducts().addAll(userInfo2.getProducts());
                                return userInfo1;
                            })).values();
        }
        log.debug("result = {}", userInfos);
        log.trace("PartyConnectorImpl.getUsers end");
        return userInfos;
    }


    @Override
    public void createUsers(String institutionId, String productId, CreateUserDto createUserDto) {
        log.trace("PartyConnectorImpl.createUsers start");
        log.debug("institutionId = {}, productId = {}, createUserDto = {}", institutionId, productId, createUserDto);
        Assert.hasText(institutionId, "An Institution id is required");
        Assert.hasText(productId, "A Product id is required");
        Assert.notNull(createUserDto, "An User is required");

        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setInstitutionId(institutionId);
        User user = new User();
        user.setProduct(productId);
        user.setName(createUserDto.getName());
        user.setSurname(createUserDto.getSurname());
        user.setTaxCode(createUserDto.getTaxCode());
        user.setEmail(createUserDto.getEmail());
        user.setProductRole(createUserDto.getProductRole());
        user.setRole(PartyRole.valueOf(createUserDto.getPartyRole()));
        onboardingRequest.setUsers(List.of(user));

        switch (user.getRole()) {
            case SUB_DELEGATE:
                restClient.onboardingSubdelegates(onboardingRequest);
                break;
            case OPERATOR:
                restClient.onboardingOperators(onboardingRequest);
                break;
            default:
                throw new IllegalArgumentException("Invalid Party role");
        }

        log.trace("PartyConnectorImpl.createUsers end");
    }


    @Override
    public void suspend(String relationshipId) {
        log.trace("PartyConnectorImpl.suspend start");
        log.debug("relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, "A Relationship id is required");
        restClient.suspendRelationship(relationshipId);
        log.trace("PartyConnectorImpl.suspend end");
    }


    @Override
    public void activate(String relationshipId) {
        log.trace("PartyConnectorImpl.activate start");
        log.debug("relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, "A Relationship id is required");
        restClient.activateRelationship(relationshipId);
        log.trace("PartyConnectorImpl.activate end");
    }


    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static class PartyProductRole implements ProductRole {
        private SelfCareAuthority selfCareRole;
        private String productRole;
        private String productId;
    }


    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static class PartyAuthInfo implements AuthInfo {
        private String institutionId;
        private Collection<ProductRole> productRoles;
    }

}
