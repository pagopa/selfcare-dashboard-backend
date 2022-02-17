package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
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
    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";

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
    static final Function<RelationshipInfo, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(relationshipInfo.getFrom());
        userInfo.setName(relationshipInfo.getName());
        userInfo.setSurname(relationshipInfo.getSurname());
        userInfo.setEmail(relationshipInfo.getEmail());
        userInfo.setStatus(relationshipInfo.getState().toString());
        userInfo.setCertified(Certification.isCertified(relationshipInfo.getCertification()));
        userInfo.setTaxCode(relationshipInfo.getTaxCode());
        userInfo.setRole(PARTY_ROLE_AUTHORITY_MAP.get(relationshipInfo.getRole()));
        it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo
                = new it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo();
        productInfo.setId(relationshipInfo.getProduct().getId());
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRelationshipId(relationshipInfo.getId());
        roleInfo.setSelcRole(PARTY_ROLE_AUTHORITY_MAP.get(relationshipInfo.getRole()));
        roleInfo.setRole(relationshipInfo.getProduct().getRole());
        roleInfo.setStatus(relationshipInfo.getState().toString());
        ArrayList<RoleInfo> roleInfos = new ArrayList<>();
        roleInfos.add(roleInfo);
        productInfo.setRoleInfos(roleInfos);
        Map<String, it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo> products = new HashMap<>();
        products.put(productInfo.getId(), productInfo);
        userInfo.setProducts(products);
        return userInfo;
    };

    private static final Function<Product, PartyProduct> PRODUCT_INFO_TO_PRODUCT_FUNCTION = productInfo -> {
        PartyProduct product = new PartyProduct();
        product.setId(productInfo.getId());
        product.setStatus(ProductStatus.valueOf(productInfo.getState().toString()));
        return product;
    };

    private static final BinaryOperator<UserInfo> USER_INFO_MERGE_FUNCTION = (userInfo1, userInfo2) -> {
        String id = userInfo2.getProducts().keySet().toArray()[0].toString();

        if (userInfo1.getProducts().containsKey(id)) {
            userInfo1.getProducts().get(id).getRoleInfos().addAll(userInfo2.getProducts().get(id).getRoleInfos());
        } else {
            userInfo1.getProducts().put(id, userInfo2.getProducts().get(id));
        }
        if (userInfo1.getStatus().equals(userInfo2.getStatus())) {
            if (userInfo1.getRole().compareTo(userInfo2.getRole()) > 0) {
                userInfo1.setRole(userInfo2.getRole());
            }
        } else {
            if ("ACTIVE".equals(userInfo2.getStatus())) {
                userInfo1.setRole(userInfo2.getRole());
                userInfo1.setStatus(userInfo2.getStatus());
            }
        }
        UserInfo result = userInfo1;
        return result;
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
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        InstitutionInfo result = parseOnBoardingInfo(onBoardingInfo).stream()
                .findAny().orElse(null);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("getInstitutions start");
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, EnumSet.of(ACTIVE, PENDING));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }


    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo) {
        log.trace("parseOnBoardingInfo start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getInstitutionId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "parseOnBoardingInfo result = {}", institutions);
        log.trace("parseOnBoardingInfo end");
        return institutions;
    }


    @Override
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);
        List<PartyProduct> products = Collections.emptyList();
        Products institutionProducts = restClient.getInstitutionProducts(institutionId, EnumSet.of(ProductState.ACTIVE, ProductState.PENDING));
        if (institutionProducts != null && institutionProducts.getProducts() != null) {
            products = institutionProducts.getProducts().stream()
                    .map(PRODUCT_INFO_TO_PRODUCT_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("getInstitutionProducts result = {}", products);
        log.trace("getInstitutionProducts end");
        return products;
    }


    @Override
    public Collection<AuthInfo> getAuthInfo(String institutionId) {
        log.trace("getAuthInfo start");
        log.debug("getAuthInfo institutionId = {}", institutionId);
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
        log.debug("getAuthInfo result = {}", authInfos);
        log.trace("getAuthInfo end");
        return authInfos;
    }


    @Override
    public Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role, Optional<String> productId, Optional<Set<String>> productRoles) {
        log.trace("getUsers start");
        log.debug("getUsers institutionId = {}, role = {}, productId = {}, productRoles = {}", institutionId, role, productId, productRoles);
        Assert.hasText(institutionId, "An Institution id is required");
        Assert.notNull(role, "An Optional role object is required");
        Assert.notNull(productId, "An Optional Product id object is required");
        Assert.notNull(productRoles, "An optional Product role is required");
        Collection<UserInfo> userInfos = Collections.emptyList();
        EnumSet<PartyRole> roles = null;
        if (role.isPresent()) {
            roles = PARTY_ROLE_AUTHORITY_MAP.entrySet().stream()
                    .filter(entry -> role.get().equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        }
        RelationshipsResponse institutionRelationships = restClient.getInstitutionRelationships(institutionId, roles, allowedStates, productId.map(Set::of).orElse(null), productRoles.orElse(null));
        if (institutionRelationships != null) {
            userInfos = institutionRelationships.stream()
                    .collect(Collectors.toMap(RelationshipInfo::getFrom,
                            RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION,
                            USER_INFO_MERGE_FUNCTION)).values();
        }
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUsers result = {}", userInfos);
        log.trace("getUsers end");
        return userInfos;
    }


    @Override
    public void createUsers(String institutionId, String productId, CreateUserDto createUserDto) {
        log.trace("createUsers start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createUsers institutionId = {}, productId = {}, createUserDto = {}", institutionId, productId, createUserDto);
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

        log.trace("createUsers end");
    }


    @Override
    public void suspend(String relationshipId) {
        log.trace("suspend start");
        log.debug("suspend relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        restClient.suspendRelationship(relationshipId);
        log.trace("suspend end");
    }


    @Override
    public void activate(String relationshipId) {
        log.trace("activate start");
        log.debug("activate relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        restClient.activateRelationship(relationshipId);
        log.trace("activate end");
    }

    @Override
    public void delete(String relationshipId) {
        log.trace("delete start");
        log.debug("delete relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        restClient.deleteRelationshipById(relationshipId);
        log.trace("delete end");
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
