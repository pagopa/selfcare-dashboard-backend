package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.DelegationRestClientMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
@RequiredArgsConstructor
class CoreConnectorImpl implements MsCoreConnector {


    private final CoreInstitutionApiRestClient coreInstitutionApiRestClient;
    private final CoreDelegationApiRestClient coreDelegationApiRestClient;
    private final CoreUserApiRestClient coreUserApiRestClient;
    private final CoreManagementApiRestClient coreManagementApiRestClient;
    private final CoreOnboardingApiRestClient coreOnboardingApiRestClient;
    private final BrokerMapper brokerMapper;
    private final InstitutionMapper institutionMapper;
    private final DelegationRestClientMapper delegationMapper;

    static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    static final String REQUIRED_PRODUCT_ID_MESSAGE = "A Product id is required";
    static final String REQUIRED_INSTITUTION_TYPE_MESSAGE = "An Institution type is required";
    static final String REQUIRED_UPDATE_RESOURCE_MESSAGE = "An Institution description is required";

    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";
    static final String REQUIRED_TOKEN_ID_MESSAGE = "A tokenId is required";
    static final String REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE = "An object of geographic taxonomy list is required";
    protected static final Function<RelationshipResult, UserInfo> RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION = relationshipInfo -> {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(relationshipInfo.getFrom());
        userInfo.setStatus(relationshipInfo.getState().toString());
        userInfo.setRole(relationshipInfo.getRole().equals(RelationshipResult.RoleEnum.OPERATOR) ? SelfCareAuthority.LIMITED : SelfCareAuthority.ADMIN);
        it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo
                = new it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo();
        productInfo.setId(relationshipInfo.getProduct().getId());
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRelationshipId(relationshipInfo.getId());
        roleInfo.setSelcRole(relationshipInfo.getRole().equals(RelationshipResult.RoleEnum.OPERATOR) ? SelfCareAuthority.LIMITED : SelfCareAuthority.ADMIN);
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

    protected static final BinaryOperator<UserInfo> USER_INFO_MERGE_FUNCTION = (userInfo1, userInfo2) -> {
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

    @Override
    public List<InstitutionInfo> getUserProducts(String userId) {
        log.trace("getUserProducts start");
        UserProductsResponse productsInfoUsingGET = coreUserApiRestClient._getUserProductsInfoUsingGET(userId, null,
                listToString(List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).getBody();

        if(Objects.isNull(productsInfoUsingGET) ||
                Objects.isNull(productsInfoUsingGET.getBindings())) return List.of();

        List<InstitutionInfo> result = productsInfoUsingGET.getBindings().stream()
                .map(institutionMapper::toInstitutionInfo)
                .toList();
        log.debug("getUserProducts result = {}", result);
        log.trace("getUserProducts end");
        return result;
    }

    @Override
    public Collection<AuthInfo> getAuthInfo(String institutionId) {
        log.trace("getAuthInfo start");
        log.debug("getAuthInfo institutionId = {}", institutionId);
        Collection<AuthInfo> authInfos = Collections.emptyList();
        OnboardingInfoResponse onBoardingInfo = coreOnboardingApiRestClient._onboardingInfoUsingGET(institutionId, null, ACTIVE.name()).getBody();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            authInfos = onBoardingInfo.getInstitutions().stream()
                    .filter(onboardingData -> onboardingData.getProductInfo() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.groupingBy(OnboardedInstitutionResponse::getId,
                                    Collectors.mapping(onboardingData -> {
                                        PartyProductRole productRole = new PartyProductRole();
                                        productRole.setProductId(onboardingData.getProductInfo().getId());
                                        productRole.setProductRole(onboardingData.getProductInfo().getRole());
                                        productRole.setPartyRole(PartyRole.valueOf(onboardingData.getRole().name()));
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
    public Institution getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        InstitutionResponse institution = coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId).getBody();
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        return institutionMapper.toInstitution(institution);
    }

    @Override
    public Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updateInstitutionResource) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, updateInstitutionResource = {}", institutionId, updateInstitutionResource);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.notNull(updateInstitutionResource, REQUIRED_UPDATE_RESOURCE_MESSAGE);
        InstitutionResponse institution = coreInstitutionApiRestClient._updateInstitutionUsingPUT(institutionId, institutionMapper.toInstitutionPut(updateInstitutionResource)).getBody();
        log.debug("updateInstitutionDescription result = {}", institution);
        log.trace("updateInstitutionDescription end");
        return institutionMapper.toInstitution(institution);
    }

    @Override
    public DelegationId createDelegation(DelegationRequest delegation) {
        log.trace("createDelegation start");
        log.debug("createDelegation request = {}", delegation.toString());
        DelegationId delegationId = new DelegationId();
        DelegationResponse result = coreDelegationApiRestClient._createDelegationUsingPOST(delegationMapper.toDelegationRequest(delegation)).getBody();
        log.debug("updateInstitutionDescription result = {}", result);
        log.trace("updateInstitutionDescription end");
        if (result != null) {
            delegationId.setId(result.getId());
        }
        return delegationId;
    }

    @Override
    public List<BrokerInfo> findInstitutionsByProductAndType(String productId, String type) {
        log.trace("findInstitutionsByProductAndType start");
        log.debug("findInstitutionsByProductAndType productId = {}, type = {}", productId, type);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        Assert.hasText(type, REQUIRED_INSTITUTION_TYPE_MESSAGE);
        List<BrokerResponse> brokerResponses = coreInstitutionApiRestClient._getInstitutionBrokersUsingGET(productId, type).getBody();
        List<BrokerInfo> brokers = brokerMapper.fromInstitutions(brokerResponses);
        log.debug("findInstitutionsByProductAndType result = {}", brokers);
        log.trace("findInstitutionsByProductAndType end");
        return brokers;
    }

    @Override
    public List<Delegation> getDelegations(String from, String to, String productId) {
        log.trace("getDelegations start");
        log.debug("getDelegations productId = {}, type = {}", from, productId);
        List<DelegationResponse> delegationsResponse = coreDelegationApiRestClient._getDelegationsUsingGET(from, to, productId, null).getBody();

        if (Objects.isNull(delegationsResponse))
            return List.of();

        List<Delegation> delegations = delegationsResponse.stream()
                .map(delegationMapper::toDelegations)
                .toList();
        log.debug("getDelegations result = {}", delegations);
        log.trace("getDelegations end");
        return delegations;
    }

    @Override
    public void updateUser(String userId, String institutionId) {
        log.trace("updateUser start");
        log.debug("updateUser userId = {}, institutionId = {}", userId, institutionId);
        coreUserApiRestClient._updateUserUsingPOST(userId, institutionId);
        log.trace("updateUser end");
    }

    @Override
    public InstitutionInfo getOnBoardedInstitution(String institutionId) {
        log.trace("getOnBoardedInstitution start");
        log.debug("getOnBoardedInstitution institutionId = {}", institutionId);
        InstitutionInfo result = null;
        OnboardingInfoResponse onBoardingInfo = coreOnboardingApiRestClient._onboardingInfoUsingGET(institutionId, null, ACTIVE.name()).getBody();
        if (onBoardingInfo != null && !CollectionUtils.isEmpty(onBoardingInfo.getInstitutions())) {
            result = institutionMapper.toInstitutionInfo(onBoardingInfo.getInstitutions()).stream().findAny().orElse(null);
        }
        log.debug("getOnBoardedInstitution result = {}", result);
        log.trace("getOnBoardedInstitution end");
        return result;
    }

    @Override
    public void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies) {
        log.trace("updateInstitutionGeographicTaxonomy start");
        log.debug("updateInstitutionGeographicTaxonomy institutionId = {}, geograpihc taxonomies = {}", institutionId, geographicTaxonomies);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.notNull(geographicTaxonomies, REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE);
        InstitutionPut geographicTaxonomiesRequest = new InstitutionPut();
        geographicTaxonomiesRequest.setGeographicTaxonomyCodes(geographicTaxonomies.getGeographicTaxonomyList().stream().map(GeographicTaxonomy::getCode).toList());
        coreInstitutionApiRestClient._updateInstitutionUsingPUT(institutionId, geographicTaxonomiesRequest);
        log.trace("updateInstitutionGeographicTaxonomy end");
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        InstitutionResponse institution = coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId).getBody();
        List<GeographicTaxonomy> result = Collections.emptyList();
        if (institution != null && !CollectionUtils.isEmpty(institution.getGeographicTaxonomies())) {
            result = institutionMapper.toGeographicTaxonomy(institution.getGeographicTaxonomies());
        }
        if (CollectionUtils.isEmpty(result)) {
            throw new ValidationException(String.format("The institution %s does not have geographic taxonomies.", institutionId));
        }

        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    public UserInfo getUser(String relationshipId) {
        log.trace("getUser start");
        log.debug("getUser = {}", relationshipId);
        RelationshipResult relationshipResult = coreUserApiRestClient._getRelationshipUsingGET(relationshipId).getBody();
        UserInfo user = RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipResult);
        log.debug("getUser result = {}", user);
        log.trace("getUser end");
        return user;
    }

    @Override
    public void createUsers(String institutionId, String productId, String userId, CreateUserDto userDto, String
            productTitle) {
        log.trace("createUsers start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createUsers institutionId = {}, productId = {}, createUserDto = {}", institutionId, productId, userId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        Assert.hasText(userId, "An User Id is required");
        Assert.notNull(userDto, "A User is required");

        OnboardingInstitutionOperatorsRequest onboardingUsersRequest = new OnboardingInstitutionOperatorsRequest();
        onboardingUsersRequest.setInstitutionId(institutionId);
        onboardingUsersRequest.setProductId(productId);
        onboardingUsersRequest.setProductTitle(productTitle);
        Map<Person.RoleEnum, List<Person>> partyRoleToUsersMap = getPartyRoleListMap(userId, userDto);

        if (partyRoleToUsersMap.size() > 1) {
            throw new ValidationException(String.format("Is not allowed to create both %s and %s users", PartyRole.SUB_DELEGATE, PartyRole.OPERATOR));
        }

        partyRoleToUsersMap.forEach((key, value) -> {
            onboardingUsersRequest.setUsers(value);
            switch (key) {
                case SUB_DELEGATE:
                    coreOnboardingApiRestClient._onboardingInstitutionSubDelegateUsingPOST(onboardingUsersRequest);
                    break;
                case OPERATOR:
                    coreOnboardingApiRestClient._onboardingInstitutionOperatorsUsingPOST(onboardingUsersRequest);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Party role");
            }
        });

        log.trace("createUsers end");
    }


    @Override
    public void checkExistingRelationshipRoles(String institutionId, String productId, CreateUserDto
            userDto, String userId) {
        log.trace("checkExistingRelationshipRoles start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "checkExistingRelationshipRoles institutionId = {}, productId = {}, createUserDto = {}, userId = {}", institutionId, productId, userDto, userId);

        Map<Person.RoleEnum, List<Person>> partyRoleToUsersMap = getPartyRoleListMap(userId, userDto);

        UserInfo.UserInfoFilterV2 userInfoFilter = new UserInfo.UserInfoFilterV2();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setUserId(userId);
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));

        List<String> states = CollectionUtils.isEmpty(userInfoFilter.getAllowedStates()) ?
                Collections.emptyList() : userInfoFilter.getAllowedStates().stream().map(Enum::name).toList();

        List<RelationshipResult> relationshipResults = coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(institutionId, userInfoFilter.getUserId(), null, listToString(states), userInfoFilter.getProductId(), listToString(userInfoFilter.getProductRoles())).getBody();
        if (!CollectionUtils.isEmpty(relationshipResults)) {
            Set<Person.RoleEnum> roles = partyRoleToUsersMap.keySet();
            List<RelationshipResult.RoleEnum> partyRoles = relationshipResults.stream().map(RelationshipResult::getRole).toList();

            if (checkUserRole(userDto, relationshipResults)) {
                throw new ValidationException("User role conflict");
            }

            if (!roles.contains(Person.RoleEnum.OPERATOR) || !(partyRoles.contains(RelationshipResult.RoleEnum.OPERATOR))) {
                throw new ValidationException("User role conflict");
            }
        }
        log.trace("checkExistingRelationshipRoles end");
    }

    private boolean checkUserRole(CreateUserDto userDto, List<RelationshipResult> relationshipResults) {
        Set<String> productRoles = relationshipResults.stream()
                .map(RelationshipResult::getProduct)
                .map(ProductInfo::getRole)
                .collect(Collectors.toSet());

        return userDto.getRoles().stream()
                .map(CreateUserDto.Role::getProductRole)
                .anyMatch(productRoles::contains);
    }


    private Map<Person.RoleEnum, List<Person>> getPartyRoleListMap(String userId, CreateUserDto userDto) {
        return userDto.getRoles().stream()
                .map(role -> {
                    Person user = new Person();
                    user.setName(userDto.getName());
                    user.setSurname(userDto.getSurname());
                    user.setTaxCode(userDto.getTaxCode());
                    user.setEmail(userDto.getEmail());
                    user.setId(userId);
                    user.setProductRole(role.getProductRole());
                    user.setRole(Person.RoleEnum.valueOf(role.getPartyRole().name()));
                    user.setRoleLabel(role.getLabel());
                    return user;
                }).collect(Collectors.groupingBy(Person::getRole));
    }


    @Override
    public void suspend(String relationshipId) {
        log.trace("suspend start");
        log.debug("suspend relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        coreUserApiRestClient._suspendRelationshipUsingPOST(relationshipId);
        log.trace("suspend end");
    }


    @Override
    public void activate(String relationshipId) {
        log.trace("activate start");
        log.debug("activate relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        coreUserApiRestClient._activateRelationshipUsingPOST(relationshipId);
        log.trace("activate end");
    }

    @Override
    public void delete(String relationshipId) {
        log.trace("delete start");
        log.debug("delete relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        coreUserApiRestClient._deleteRelationshipUsingDELETE(relationshipId);
        log.trace("delete end");
    }

    @Override
    public OnboardingRequestInfo getOnboardingRequestInfo(String tokenId) {
        log.trace("getOnboardingRequestInfo start");
        log.debug("getOnboardingRequestInfo tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        final OnboardingRequestInfo onboardingRequestInfo = new OnboardingRequestInfo();
        onboardingRequestInfo.setAdmins(new ArrayList<>());
        TokenResponse tokenInfo = coreManagementApiRestClient._getTokenUsingGET(tokenId).getBody();
        if (tokenInfo != null) {
            onboardingRequestInfo.setProductId(tokenInfo.getProductId());
            tokenInfo.getLegals().forEach(relationshipBinding -> {
                final UserInfo userInfo = new UserInfo();
                userInfo.setId(relationshipBinding.getPartyId());
                userInfo.setStatus(relationshipBinding.getRole().toString());
                userInfo.setRole(relationshipBinding.getRole().equals(LegalsResponse.RoleEnum.OPERATOR) ? SelfCareAuthority.LIMITED : SelfCareAuthority.ADMIN);
                if (LegalsResponse.RoleEnum.MANAGER.equals(relationshipBinding.getRole())) {
                    onboardingRequestInfo.setManager(userInfo);
                } else {
                    onboardingRequestInfo.getAdmins().add(userInfo);
                }
            });
            InstitutionResponse institutionResponse = coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(tokenInfo.getInstitutionId()).getBody();
            InstitutionInfo institutionInfo = institutionMapper.toInstitutionInfo(institutionResponse, tokenInfo.getInstitutionUpdate());
            institutionInfo.setStatus(RelationshipState.valueOf(tokenInfo.getStatus().name()));
            if (institutionResponse != null && !CollectionUtils.isEmpty(institutionResponse.getOnboarding())) {
                institutionResponse.getOnboarding().stream()
                        .filter(onboarding -> onboarding.getProductId().equals(tokenInfo.getProductId()))
                        .findFirst()
                        .ifPresent(onboardedProductResponse -> institutionInfo.setBilling(institutionMapper.toBilling(onboardedProductResponse.getBilling())));
            }
            onboardingRequestInfo.setInstitutionInfo(institutionInfo);
            log.debug("getOnboardingRequestInfo result = {}", onboardingRequestInfo);
            log.trace("getOnboardingRequestInfo end");
        }
        return onboardingRequestInfo;
    }

    @Override
    public void approveOnboardingRequest(String tokenId) {
        log.trace("approveOnboardingRequest start");
        log.debug("approveOnboardingRequest tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        coreOnboardingApiRestClient._approveOnboardingUsingPOST(tokenId);
        log.trace("retrieveOnboardingRequest end");
    }

    @Override
    public void rejectOnboardingRequest(String tokenId) {
        log.trace("rejectOnboardingRequest start");
        log.debug("rejectOnboardingRequest tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        coreOnboardingApiRestClient._onboardingRejectUsingDELETE(tokenId);
        log.trace("rejectOnboardingRequest end");
    }

    @Override
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);
        List<PartyProduct> products = Collections.emptyList();
        OnboardedProducts institutionProducts = coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() + "," + ProductState.PENDING.name()).getBody();
        if (institutionProducts != null && !CollectionUtils.isEmpty(institutionProducts.getProducts())) {
            products = institutionProducts.getProducts().stream()
                    .map(institutionMapper::toPartyProduct)
                    .toList();
        }
        log.debug("getInstitutionProducts result = {}", products);
        log.trace("getInstitutionProducts end");
        return products;
    }

    @Override
    public Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilterV2 userInfoFilter) {
        log.trace("getUsers start");
        log.debug("getUsers institutionId = {}, role = {}, productId = {}, productRoles = {}, userId = {}", institutionId, userInfoFilter.getRole(), userInfoFilter.getProductId(), userInfoFilter.getProductRoles(), userInfoFilter.getUserId());
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);

        Collection<UserInfo> userInfos = Collections.emptyList();
        List<String> roles = null;
        if (userInfoFilter.getRole() != null) {
            roles = Arrays.stream(PartyRole.values())
                    .filter(partyRole -> partyRole.getSelfCareAuthority().equals(userInfoFilter.getRole()))
                    .map(Enum::name)
                    .toList();
        }
        List<RelationshipResult> institutionRelationships = coreInstitutionApiRestClient._getUserInstitutionRelationshipsUsingGET(institutionId,
                        userInfoFilter.getUserId(),
                        listToString(roles),
                        !CollectionUtils.isEmpty(userInfoFilter.getAllowedStates()) ? listToString(userInfoFilter.getAllowedStates().stream().map(Enum::name).toList()) : null,
                        StringUtils.hasText(userInfoFilter.getProductId()) ? listToString(List.of(userInfoFilter.getProductId())) : null,
                        listToString(userInfoFilter.getProductRoles()))
                .getBody();

        if (institutionRelationships != null) {
            userInfos = institutionRelationships.stream()
                    .collect(Collectors.toMap(RelationshipResult::getFrom,
                            RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION,
                            USER_INFO_MERGE_FUNCTION)).values();
        }
        log.debug("getUsers result = {}", userInfos);
        log.trace("getUsers end");
        return userInfos;
    }


    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected static class PartyProductRole implements ProductRole {
        protected String productRole;
        protected String productId;
        protected PartyRole partyRole;
    }


    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected static class PartyAuthInfo implements AuthInfo {
        protected String institutionId;
        protected Collection<ProductRole> productRoles;
    }

    private String listToString(List<String> list){
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        return String.join(",", list);
    }


}
