package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyManagementRestClient;
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
import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.Relationship;
import it.pagopa.selfcare.dashboard.connector.rest.model.token.TokenInfo;
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

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
class PartyConnectorImpl implements PartyConnector {

    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";
    private static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    static final String REQUIRED_TOKEN_ID_MESSAGE = "A tokenId is required";

    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION = (inst1, inst2) -> {
                if(ACTIVE.equals(inst1.getStatus())){
                    return inst1;
                } else if (PENDING.equals(inst1.getStatus())){
                    return inst1;
                } else {
                    return inst2;
                }
            };
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
        institutionInfo.setStatus(onboardingData.getState());
        institutionInfo.setAddress(onboardingData.getAddress());
        institutionInfo.setZipCode(onboardingData.getZipCode());
        institutionInfo.setBilling(onboardingData.getBilling());
        if (onboardingData.getGeographicTaxonomies() == null) {
            throw new ValidationException(String.format("The institution %s does not have geographic taxonomies.", institutionInfo.getId()));
        } else {
            institutionInfo.setGeographicTaxonomies(onboardingData.getGeographicTaxonomies());
        }
        if (onboardingData.getAttributes() != null && !onboardingData.getAttributes().isEmpty()) {
            institutionInfo.setCategory(onboardingData.getAttributes().get(0).getDescription());
        }
        return institutionInfo;
    };

    static final Function<Relationship, InstitutionInfo> RELATIONSHIP_TO_INSTITUTION_INFO_FUNCTION = relationship -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setId(relationship.getTo().toString());
        institutionInfo.setStatus(relationship.getState());
        institutionInfo.setInstitutionType(relationship.getInstitutionUpdate().getInstitutionType());
        institutionInfo.setDescription(relationship.getInstitutionUpdate().getDescription());
        institutionInfo.setTaxCode(relationship.getInstitutionUpdate().getTaxCode());
        institutionInfo.setDigitalAddress(relationship.getInstitutionUpdate().getDigitalAddress());
        institutionInfo.setAddress(relationship.getInstitutionUpdate().getAddress());
        institutionInfo.setZipCode(relationship.getInstitutionUpdate().getZipCode());
        institutionInfo.setPaymentServiceProvider(relationship.getInstitutionUpdate().getPaymentServiceProvider());
        institutionInfo.setDataProtectionOfficer(relationship.getInstitutionUpdate().getDataProtectionOfficer());
        institutionInfo.setBilling(relationship.getBilling());
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
        product.setOnBoardingStatus(ProductOnBoardingStatus.valueOf(productInfo.getState().toString()));
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

    private final PartyProcessRestClient partyProcessRestClient;
    private final PartyManagementRestClient partyManagementRestClient;


    @Autowired
    public PartyConnectorImpl(PartyProcessRestClient partyProcessRestClient,
                              PartyManagementRestClient partyManagementRestClient) {
        this.partyProcessRestClient = partyProcessRestClient;
        this.partyManagementRestClient = partyManagementRestClient;
    }


    @Override
    public InstitutionInfo getOnBoardedInstitution(String institutionId) {
        log.trace("getOnBoardedInstitution start");
        log.debug("getOnBoardedInstitution institutionId = {}", institutionId);
        OnBoardingInfo onBoardingInfo = partyProcessRestClient.getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
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
        RelationshipInfo relationshipInfo = partyProcessRestClient.getRelationship(relationshipId);
        UserInfo user = RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        log.debug("getUser result = {}", user);
        log.trace("getUser end");
        return user;
    }


    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions() {
        log.trace("getOnBoardedInstitutions start");
        OnBoardingInfo onBoardingInfo = partyProcessRestClient.getOnBoardingInfo(null, null, EnumSet.of(ACTIVE, PENDING, TOBEVALIDATED));
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
        Products institutionProducts = partyProcessRestClient.getInstitutionProducts(institutionId, EnumSet.of(ProductState.ACTIVE, ProductState.PENDING));
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
        OnBoardingInfo onBoardingInfo = partyProcessRestClient.getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
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
        RelationshipsResponse institutionRelationships = partyProcessRestClient.getUserInstitutionRelationships(institutionId, roles, userInfoFilter.getAllowedStates().orElse(null), userInfoFilter.getProductId().map(Set::of).orElse(null), userInfoFilter.getProductRoles().orElse(null), userInfoFilter.getUserId().orElse(null));
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
        Map<PartyRole, List<User>> partyRoleToUsersMap = getPartyRoleListMap(userId, userDto);

        if (partyRoleToUsersMap.size() > 1) {
            throw new ValidationException(String.format("Is not allowed to create both %s and %s users", PartyRole.SUB_DELEGATE, PartyRole.OPERATOR));
        }

        partyRoleToUsersMap.forEach((key, value) -> {
            onboardingUsersRequest.setUsers(value);
            switch (key) {
                case SUB_DELEGATE:
                    partyProcessRestClient.onboardingSubdelegates(onboardingUsersRequest);
                    break;
                case OPERATOR:
                    partyProcessRestClient.onboardingOperators(onboardingUsersRequest);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Party role");
            }
        });

        log.trace("createUsers end");
    }


    @Override
    public void checkExistingRelationshipRoles(String institutionId, String productId, CreateUserDto userDto, String userId) {
        log.trace("checkExistingRelationshipRoles start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "checkExistingRelationshipRoles institutionId = {}, productId = {}, createUserDto = {}, userId = {}", institutionId, productId, userDto, userId);

        Map<PartyRole, List<User>> partyRoleToUsersMap = getPartyRoleListMap(userId, userDto);

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(productId));
        userInfoFilter.setUserId(Optional.ofNullable(userId));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        RelationshipsResponse institutionRelationships = partyProcessRestClient.getUserInstitutionRelationships(institutionId, EnumSet.allOf(PartyRole.class), userInfoFilter.getAllowedStates().orElse(null), userInfoFilter.getProductId().map(Set::of).orElse(null), userInfoFilter.getProductRoles().orElse(null), userInfoFilter.getUserId().orElse(null));
        if (!institutionRelationships.isEmpty()) {
            Set<PartyRole> roles = partyRoleToUsersMap.keySet();
            List<PartyRole> partyRoles = institutionRelationships.stream().map(RelationshipInfo::getRole).collect(Collectors.toList());

            if (!roles.contains(PartyRole.OPERATOR) || !(partyRoles.contains(PartyRole.OPERATOR))) {
                throw new ValidationException("User role conflict");
            }
        }
        log.trace("checkExistingRelationshipRoles end");
    }


    private Map<PartyRole, List<User>> getPartyRoleListMap(String userId, CreateUserDto userDto) {
        return userDto.getRoles().stream()
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
    }


    @Override
    public void suspend(String relationshipId) {
        log.trace("suspend start");
        log.debug("suspend relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        partyProcessRestClient.suspendRelationship(relationshipId);
        log.trace("suspend end");
    }


    @Override
    public void activate(String relationshipId) {
        log.trace("activate start");
        log.debug("activate relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        partyProcessRestClient.activateRelationship(relationshipId);
        log.trace("activate end");
    }

    @Override
    public void delete(String relationshipId) {
        log.trace("delete start");
        log.debug("delete relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        partyProcessRestClient.deleteRelationshipById(relationshipId);
        log.trace("delete end");
    }


    @Override
    public Institution getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution institution = partyProcessRestClient.getInstitution(institutionId);
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        return institution;
    }


    @Override
    public Institution getInstitutionByExternalId(String institutionExternalId) {
        log.trace("getInstitutionByExternalId start");
        log.debug("getInstitutionByExternalId institutionExternalId = {}", institutionExternalId);
        Assert.hasText(institutionExternalId, "An Institution external id is required");
        Institution institution = partyProcessRestClient.getInstitutionByExternalId(institutionExternalId);
        log.debug("getInstitutionByExternalId result = {}", institution);
        log.trace("getInstitutionByExternalId end");
        return institution;
    }


    @Override
    public OnboardingRequestInfo getOnboardingRequestInfo(String tokenId) {
        log.trace("getOnboardingRequestInfo start");
        log.debug("getOnboardingRequestInfo tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        final OnboardingRequestInfo onboardingRequestInfo = new OnboardingRequestInfo();
        onboardingRequestInfo.setAdmins(new ArrayList<>());
        final TokenInfo tokenInfo = partyManagementRestClient.getToken(UUID.fromString(tokenId));
        tokenInfo.getLegals().forEach(relationshipBinding -> {
            final UserInfo userInfo = new UserInfo();
            userInfo.setId(relationshipBinding.getPartyId().toString());
            userInfo.setStatus(relationshipBinding.getRole().toString());
            userInfo.setRole(relationshipBinding.getRole().getSelfCareAuthority());
            if (PartyRole.MANAGER.equals(relationshipBinding.getRole())) {
                onboardingRequestInfo.setManager(userInfo);
                final Relationship relationship = partyManagementRestClient.getRelationshipById(relationshipBinding.getRelationshipId());
                InstitutionInfo institutionInfo = RELATIONSHIP_TO_INSTITUTION_INFO_FUNCTION.apply(relationship);
                onboardingRequestInfo.setInstitutionInfo(institutionInfo);
                Institution institution = partyManagementRestClient.getInstitutionByExternalId(institutionInfo.getTaxCode());
                if (institution == null) {
                    throw new ResourceNotFoundException(String.format("Institution %s not found", institutionInfo.getTaxCode()));
                }
                onboardingRequestInfo.getInstitutionInfo().setGeographicTaxonomies(institution.getGeographicTaxonomies());

            } else {
                onboardingRequestInfo.getAdmins().add(userInfo);
            }

        });
        log.debug("getOnboardingRequestInfo result = {}", onboardingRequestInfo);
        log.trace("getOnboardingRequestInfo end");
        return onboardingRequestInfo;
    }

    @Override
    public void approveOnboardingRequest(String tokenId) {
        log.trace("approveOnboardingRequest start");
        log.debug("approveOnboardingRequest tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        partyProcessRestClient.approveOnboardingRequest(tokenId);
        log.trace("retrieveOnboardingRequest end");
    }

    @Override
    public void rejectOnboardingRequest(String tokenId){
        log.trace("rejectOnboardingRequest start");
        log.debug("rejectOnboardingRequest tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        partyProcessRestClient.rejectOnboardingRequest(tokenId);
        log.trace("rejectOnboardingRequest end");
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
