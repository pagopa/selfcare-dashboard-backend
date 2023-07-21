package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.UserProductsResponse;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreUserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static it.pagopa.selfcare.dashboard.connector.rest.PartyConnectorImpl.*;

@Slf4j
@Service
class MsCoreConnectorImpl implements MsCoreConnector {

    static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    static final String REQUIRED_UPDATE_RESOURCE_MESSAGE = "An Institution description is required";

    private final MsCoreRestClient msCoreRestClient;
    private final MsCoreUserApiRestClient msCoreUserApiRestClient;

    private final InstitutionMapper institutionMapper;


    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient msCoreRestClient, MsCoreUserApiRestClient msCoreUserApiRestClient, InstitutionMapper institutionMapper) {
        this.msCoreRestClient = msCoreRestClient;
        this.msCoreUserApiRestClient = msCoreUserApiRestClient;
        this.institutionMapper = institutionMapper;
    }

    @Deprecated
    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions() {
        log.trace("getOnBoardedInstitutions start");
        OnBoardingInfo onBoardingInfo = msCoreRestClient.getOnBoardingInfo(null, null, EnumSet.of(ACTIVE, PENDING, TOBEVALIDATED));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }
    @Override
    public List<InstitutionInfo> getUserProducts(String userId) {
        log.trace("getUserProducts start");
        UserProductsResponse productsInfoUsingGET = msCoreUserApiRestClient._getUserProductsInfoUsingGET(userId, null,
                String.join(",", ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name())).getBody();

        if(Objects.isNull(productsInfoUsingGET) ||
                Objects.isNull(productsInfoUsingGET.getBindings())) return List.of();

        List<InstitutionInfo> result = productsInfoUsingGET.getBindings().stream()
                .map(institutionMapper::toInstitutionInfo)
                .collect(Collectors.toList());
        log.debug("getUserProducts result = {}", result);
        log.trace("getUserProducts end");
        return result;
    }

    @Override
    public UserInfo getUser(String relationshipId) {
        log.trace("getUser start");
        log.debug("getUser = {}", relationshipId);
        RelationshipInfo relationshipInfo = msCoreRestClient.getRelationship(relationshipId);
        UserInfo user = RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        log.debug("getUser result = {}", user);
        log.trace("getUser end");
        return user;
    }

    @Override
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);
        List<PartyProduct> products = Collections.emptyList();
        Products institutionProducts = msCoreRestClient.getInstitutionProducts(institutionId, EnumSet.of(ProductState.ACTIVE, ProductState.PENDING));
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
        OnBoardingInfo onBoardingInfo = msCoreRestClient.getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
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
        RelationshipsResponse institutionRelationships = msCoreRestClient.getUserInstitutionRelationships(institutionId, roles, userInfoFilter.getAllowedStates().orElse(null), userInfoFilter.getProductId().map(Set::of).orElse(null), userInfoFilter.getProductRoles().orElse(null), userInfoFilter.getUserId().orElse(null));
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
    public Institution getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution institution = msCoreRestClient.getInstitution(institutionId);
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        return institution;
    }

    @Override
    public Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updateInstitutionResource) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, updateInstitutionResource = {}", institutionId, updateInstitutionResource);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.notNull(updateInstitutionResource, REQUIRED_UPDATE_RESOURCE_MESSAGE);
        Institution institution = msCoreRestClient.updateInstitutionDescription(institutionId, updateInstitutionResource);
        log.debug("updateInstitutionDescription result = {}", institution);
        log.trace("updateInstitutionDescription end");
        return institution;
    }

}
