package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.User;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState.PENDING;

@Slf4j
@Service
class PartyConnectorImpl implements PartyConnector {

    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";
    private static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";

    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION =
            (inst1, inst2) -> ACTIVE.name().equals(inst1.getStatus()) ? inst1 : inst2;
    private static final Function<OnboardingData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setOriginId(onboardingData.getOriginId());
        institutionInfo.setId(onboardingData.getId());
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        institutionInfo.setExternalId(onboardingData.getExternalId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setStatus(onboardingData.getState().toString());
        institutionInfo.setAddress(onboardingData.getAddress());
        if (onboardingData.getAttributes() != null && !onboardingData.getAttributes().isEmpty()) {
            institutionInfo.setCategory(onboardingData.getAttributes().get(0).getDescription());
        }
        return institutionInfo;
    };
    static final Function<RelationshipInfo, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(relationshipInfo.getFrom());
        userInfo.setStatus(relationshipInfo.getState().toString());
        userInfo.setRole(relationshipInfo.getRole().getSelfCareAuthority());
        it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo
                = new it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo();
        productInfo.setId(relationshipInfo.getProduct().getId());
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRelationshipId(relationshipInfo.getId());
        roleInfo.setSelcRole(relationshipInfo.getRole().getSelfCareAuthority());
        roleInfo.setRole(relationshipInfo.getProduct().getRole());
        roleInfo.setStatus(relationshipInfo.getState().toString());
        ArrayList<RoleInfo> roleInfos = new ArrayList<>();
        roleInfos.add(roleInfo);
        productInfo.setRoleInfos(roleInfos);
        Map<String, it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo> products = new HashMap<>();
        products.put(productInfo.getId(), productInfo);
        userInfo.setProducts(products);
        userInfo.setInstitutionId(relationshipInfo.getTo());
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
        return userInfo1;
    };

    private final PartyProcessRestClient restClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public InstitutionInfo getOnBoardedInstitution(String institutionId) {
        log.trace("getOnBoardedInstitution start");
        log.debug("getOnBoardedInstitution institutionId = {}", institutionId);
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        InstitutionInfo result = parseOnBoardingInfo(onBoardingInfo).stream()
                .findAny().orElse(null);
        log.debug("getOnBoardedInstitution result = {}", result);
        log.trace("getOnBoardedInstitution end");
        return result;
    }


    @Override
    public UserInfo getUser(String relationshipId) {
        log.trace("getUser start");
        log.debug("getUser = {}", relationshipId);
        RelationshipInfo relationshipInfo = restClient.getRelationship(relationshipId);
        UserInfo user = RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        log.debug("getUser result = {}", user);
        log.trace("getUser end");
        return user;
    }


    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions() {
        log.trace("getOnBoardedInstitutions start");
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(null, null, EnumSet.of(ACTIVE, PENDING));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }


    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingInfo onBoardingInfo) {
        log.trace("parseOnBoardingInfo start");
        log.debug("parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug("parseOnBoardingInfo result = {}", institutions);
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
        OnBoardingInfo onBoardingInfo = restClient.getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            authInfos = onBoardingInfo.getInstitutions().stream()
                    .filter(onboardingData -> onboardingData.getProductInfo() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.groupingBy(OnboardingData::getId,
                                    Collectors.mapping(onboardingData -> {
                                        PartyProductRole productRole = new PartyProductRole();
                                        productRole.setProductId(onboardingData.getProductInfo().getId());
                                        productRole.setProductRole(onboardingData.getProductInfo().getRole());
                                        productRole.setPartyRole(onboardingData.getRole());
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
    public Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getUsers start");
        log.debug("getUsers institutionId = {}, role = {}, productId = {}, productRoles = {}, userId = {}", institutionId, userInfoFilter.getRole(), userInfoFilter.getProductId(), userInfoFilter.getProductRoles(), userInfoFilter.getUserId());
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);

        Collection<UserInfo> userInfos = Collections.emptyList();
        EnumSet<PartyRole> roles = null;
        if (userInfoFilter.getRole().isPresent()) {
            roles = Arrays.stream(PartyRole.values())
                    .filter(partyRole -> partyRole.getSelfCareAuthority().equals(userInfoFilter.getRole().get()))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartyRole.class)));
        }
        RelationshipsResponse institutionRelationships = restClient.getUserInstitutionRelationships(institutionId, roles, userInfoFilter.getAllowedStates().orElse(null), userInfoFilter.getProductId().map(Set::of).orElse(null), userInfoFilter.getProductRoles().orElse(null), userInfoFilter.getUserId().orElse(null));
        if (institutionRelationships != null) {
            userInfos = institutionRelationships.stream()
                    .collect(Collectors.toMap(RelationshipInfo::getFrom,
                            RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION,
                            USER_INFO_MERGE_FUNCTION)).values();
        }
        log.debug("getUsers result = {}", userInfos);
        log.trace("getUsers end");
        return userInfos;
    }


    @Override
    public void createUsers(String institutionId, String productId, String userId, CreateUserDto userDto) {
        log.trace("createUsers start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createUsers institutionId = {}, productId = {}, createUserDto = {}", institutionId, productId, userId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, "A Product id is required");
        Assert.hasText(userId, "An User Id is required");
        Assert.notNull(userDto, "A User is required");

        OnboardingUsersRequest onboardingUsersRequest = new OnboardingUsersRequest();
        onboardingUsersRequest.setInstitutionId(institutionId);
        onboardingUsersRequest.setProductId(productId);
        Map<PartyRole, List<User>> partyRoleToUsersMap = userDto.getRoles().stream()
                .map(role -> {
                    User user = new User();
                    user.setName(userDto.getName());
                    user.setSurname(userDto.getSurname());
                    user.setTaxCode(userDto.getTaxCode());
                    user.setEmail(userDto.getEmail());
                    user.setId(UUID.fromString(userId));
                    user.setProductRole(role.getProductRole());
                    user.setRole(role.getPartyRole());
                    return user;
                }).collect(Collectors.groupingBy(User::getRole));

        if (partyRoleToUsersMap.size() > 1) {
            throw new ValidationException(String.format("Is not allowed to create both %s and %s users", PartyRole.SUB_DELEGATE, PartyRole.OPERATOR));
        }

        partyRoleToUsersMap.forEach((key, value) -> {
            onboardingUsersRequest.setUsers(value);
            switch (key) {
                case SUB_DELEGATE:
                    restClient.onboardingSubdelegates(onboardingUsersRequest);
                    break;
                case OPERATOR:
                    restClient.onboardingOperators(onboardingUsersRequest);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Party role");
            }
        });

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


    @Override
    public Institution getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution institution = restClient.getInstitution(institutionId);
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        return institution;
    }


    @Override
    public Institution getInstitutionByExternalId(String institutionExternalId) {
        log.trace("getInstitutionByExternalId start");
        log.debug("getInstitutionByExternalId institutionExternalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, "An Institution external id is required");
        Institution institution = restClient.getInstitutionByExternalId(institutionExternalId);
        log.debug("getInstitutionByExternalId result = {}", institution);
        log.trace("getInstitutionByExternalId end");
        return institution;
    }


    @Setter(AccessLevel.PRIVATE)
    private static class PartyProductRole implements ProductRole {
        @Getter
        private String productRole;
        @Getter
        private String productId;
        @Getter
        private PartyRole partyRole;
    }


    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static class PartyAuthInfo implements AuthInfo {
        private String institutionId;
        private Collection<ProductRole> productRoles;
    }

}
