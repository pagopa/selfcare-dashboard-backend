package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
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

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
class MsCoreConnectorImpl implements MsCoreConnector {

    static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";

    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION = (inst1, inst2) -> {
        if (ACTIVE.equals(inst1.getStatus())) {
            return inst1;
        } else if (PENDING.equals(inst1.getStatus())) {
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
        institutionInfo.setSupportContact(onboardingData.getSupportContact());
        institutionInfo.setPaymentServiceProvider(onboardingData.getPaymentServiceProvider());
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


    private final MsCoreRestClient msCoreRestClient;


    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient msCoreRestClient) {
        this.msCoreRestClient = msCoreRestClient;
    }

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
    public UserInfo getUser(String relationshipId) {
        log.trace("getUser start");
        log.debug("getUser = {}", relationshipId);
        RelationshipInfo relationshipInfo = msCoreRestClient.getRelationship(relationshipId);
        UserInfo user = RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        log.debug("getUser result = {}", user);
        log.trace("getUser end");
        return user;
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
