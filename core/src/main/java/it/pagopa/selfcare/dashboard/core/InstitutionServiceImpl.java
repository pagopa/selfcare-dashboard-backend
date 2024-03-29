package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.*;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private static final String REQUIRED_USER_ID = "A user id is required";
    private static final EnumSet<PartyRole> PARTY_ROLE_WHITE_LIST = EnumSet.of(PartyRole.SUB_DELEGATE, PartyRole.OPERATOR);
    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final EnumSet<User.Fields> USER_FIELD_LIST_ENHANCED = EnumSet.of(fiscalCode, name, familyName, workContacts);
    private static final String A_PRODUCT_ID_IS_REQUIRED = "A Product id is required";
    private static final String A_USER_ID_IS_REQUIRED = "A User id is required";
    private static final String AN_USER_IS_REQUIRED = "An User is required";
    private static final String AN_OPTIONAL_ROLE_OBJECT_IS_REQUIRED = "An Optional role object is required";
    private static final String AN_OPTIONAL_PRODUCT_ROLE_OBJECT_IS_REQUIRED = "An Optional product role object is required";
    private static final String A_USER_INFO_FILTER_OBJECT_IS_REQUIRED = "A UserInfoFilter object is required";
    static final String REQUIRED_GEOGRAPHIC_TAXONOMIES = "An object of geographic taxonomy list is required";
    static final String REQUIRED_TOKEN_ID_MESSAGE = "A tokenId is required";
    static final String REQUIRED_UPDATE_RESOURCE_MESSAGE = "An Institution update resource is required";

    private final Optional<EnumSet<RelationshipState>> allowedStates;
    private final UserRegistryConnector userRegistryConnector;
    private final MsCoreConnector msCoreConnector;
    private final ProductsConnector productsConnector;

    protected static final BinaryOperator<PartyProduct> MERGE_FUNCTION = (inst1, inst2) -> inst1.getOnBoardingStatus().compareTo(inst2.getOnBoardingStatus()) < 0 ? inst1 : inst2;


    @Autowired
    public InstitutionServiceImpl(@Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates,
                                  UserRegistryConnector userRegistryConnector,
                                  ProductsConnector productsConnector,
                                  MsCoreConnector msCoreConnector) {
        this.allowedStates = allowedStates == null || allowedStates.length == 0
                ? Optional.empty()
                : Optional.of(EnumSet.copyOf(Arrays.stream(allowedStates)
                .map(RelationshipState::valueOf)
                .collect(Collectors.toList())));
        this.userRegistryConnector = userRegistryConnector;
        this.productsConnector = productsConnector;
        this.msCoreConnector = msCoreConnector;
    }


    @Override
    public InstitutionInfo getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        InstitutionInfo result = msCoreConnector.getOnBoardedInstitution(institutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies) {
        log.trace("updateInstitutionGeographicTaxonomy start");
        log.debug("updateInstitutionGeographicTaxonomy institutiondId = {}, geographic taxonomies = {}", institutionId, geographicTaxonomies);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(geographicTaxonomies, REQUIRED_GEOGRAPHIC_TAXONOMIES);
        msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        log.trace("updateInstitutionGeographicTaxonomy end");
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList externalInstitutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        List<GeographicTaxonomy> result = msCoreConnector.getGeographicTaxonomyList(institutionId);
        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    public Collection<InstitutionInfo> getInstitutions(String userId) {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = msCoreConnector.getUserProducts(userId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public List<ProductTree> getProductsTree(){
        log.trace("getProductsTree start");
        List<ProductTree> productTrees = productsConnector.getProductsTree();
        log.debug("getInstitutionProducts result = {}", productTrees);
        log.trace("getInstitutionProducts end");
        return productTrees;
    }

    /**
     * @deprecated method has been deprecated because a new method has been implemented.
     * Remove the query from the repository
     */
    @Deprecated(forRemoval = true)
    @Override
    public Collection<UserInfo> getInstitutionUsers(String institutionId, Optional<String> productId, Optional<SelfCareAuthority> role, Optional<Set<String>> productRoles) {
        log.trace("getInstitutionUsers start");
        log.debug("getInstitutionUsers institutionId = {}, productId = {}, role = {}, productRoles = {}", institutionId, productId, role, productRoles);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(productId, "An Optional Product id object is required");
        Assert.notNull(role, AN_OPTIONAL_ROLE_OBJECT_IS_REQUIRED);
        Assert.notNull(productRoles, AN_OPTIONAL_PRODUCT_ROLE_OBJECT_IS_REQUIRED);

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(role.orElse(null));
        userInfoFilter.setProductId(productId.orElse(null));
        userInfoFilter.setProductRoles(productRoles.map(relationshipStates -> relationshipStates.stream().toList()).orElse(null));
        userInfoFilter.setAllowedStates(allowedStates.map(relationshipStates -> relationshipStates.stream().toList()).orElse(null));
        Collection<UserInfo> userInfos = getInstitutionUsers(institutionId, userInfoFilter);
        userInfos.forEach(userInfo -> userInfo.setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST)));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionUsers result = {}", userInfos);
        log.trace("getInstitutionUsers end");
        return userInfos;
    }


    private Collection<UserInfo> getInstitutionUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter) {
        log.trace("getInstitutionUsers start");
        log.debug("getInstitutionUsers institutionId = {}, productId = {}, role = {}, productRoles = {}",
                institutionId, userInfoFilter.getProductId(), userInfoFilter.getRole(), userInfoFilter.getProductRoles());
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(userInfoFilter, A_USER_INFO_FILTER_OBJECT_IS_REQUIRED);


        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        Map<String, ProductTree> idToProductMap = productsConnector.getProductsTree().stream()
                .collect(Collectors.toMap(productTree -> productTree.getNode().getId(), Function.identity()));

        userInfos.forEach(userInfo -> {
            Iterator<Map.Entry<String, ProductInfo>> productsIterator = userInfo.getProducts().entrySet().iterator();
            while (productsIterator.hasNext()) {
                Map.Entry<String, ProductInfo> next = productsIterator.next();
                String key = next.getKey();
                ProductInfo prod = next.getValue();
                if (idToProductMap.containsKey(prod.getId())) {
                    userInfo.getProducts().get(key).setTitle(idToProductMap.get(prod.getId()).getNode().getTitle());
                } else if (idToProductMap.values().stream()
                        .map(ProductTree::getChildren)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .map(Product::getId)
                        .anyMatch(key::equals)) {
                    productsIterator.remove();
                } else {
                    productsIterator.remove();
                    log.warn("No matching product found with id {}", key);
                }
            }
        });

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionUsers result = {}", userInfos);
        log.trace("getInstitutionUsers end");
        return userInfos;
    }


    @Override
    public UserInfo getInstitutionUser(String institutionId, String userId) {
        log.trace("getInstitutionUser start");
        log.debug("getInstitutionUser institutionId = {}, userId = {}", institutionId, userId);

        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(userId, REQUIRED_USER_ID);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        userInfoFilter.setAllowedStates(allowedStates.map(relationshipStates -> relationshipStates.stream().toList()).orElse(null));

        Collection<UserInfo> userInfos = getInstitutionUsers(institutionId, userInfoFilter);
        if (!userInfos.iterator().hasNext()) {
            throw new ResourceNotFoundException("No User found for the given userId");
        }
        UserInfo result = userInfos.iterator().next();
        User user = userRegistryConnector.getUserByInternalId(result.getId(), USER_FIELD_LIST_ENHANCED);
        result.setUser(user);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionUser result = {}", result);
        log.trace("getInstitutionUser end");
        return result;
    }

    @Override
    public Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<SelfCareAuthority> role, Optional<Set<String>> productRoles) {
        log.trace("getInstitutionProductUsers start");
        log.debug("getInstitutionProductUsers institutionId = {}, productId = {}, role = {}, productRoles = {}", institutionId, productId, role, productRoles);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, A_PRODUCT_ID_IS_REQUIRED);
        Assert.notNull(role, AN_OPTIONAL_ROLE_OBJECT_IS_REQUIRED);
        Assert.notNull(productRoles, AN_OPTIONAL_PRODUCT_ROLE_OBJECT_IS_REQUIRED);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(role.orElse(null));
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles.map(relationshipStates -> relationshipStates.stream().toList()).orElse(null));
        userInfoFilter.setAllowedStates(allowedStates.map(relationshipStates -> relationshipStates.stream().toList()).orElse(null));
        Collection<UserInfo> result = msCoreConnector.getUsers(institutionId, userInfoFilter);
        result.forEach(userInfo ->
                userInfo.setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST)));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionProductUsers result = {}", result);
        log.trace("getInstitutionProductUsers end");
        return result;
    }


    @Override
    public UserId createUsers(String institutionId, String productId, CreateUserDto user) {
        log.trace("createUsers start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createUsers institutionId = {}, productId = {}, user = {}", institutionId, productId, user);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, A_PRODUCT_ID_IS_REQUIRED);
        Assert.notNull(user, AN_USER_IS_REQUIRED);

        Product product = productsConnector.getProduct(productId);
        user.getRoles().forEach(role -> {
            EnumMap<PartyRole, ProductRoleInfo> roleMappings = product.getRoleMappings();
            role.setLabel(Product.getLabel(role.getProductRole(), roleMappings).orElse(null));
            Optional<PartyRole> partyRole = Product.getPartyRole(role.getProductRole(), roleMappings, PARTY_ROLE_WHITE_LIST);
            role.setPartyRole(partyRole.orElseThrow(() ->
                    new InvalidProductRoleException(String.format("Product role '%s' is not valid", role.getProductRole()))));
        });

        UserId userId = userRegistryConnector.saveUser(user.getUser());
        msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, user, userId.getId().toString());
        msCoreConnector.createUsers(institutionId, productId, userId.getId().toString(), user, product.getTitle());
        log.debug("createUsers result = {}", userId);
        log.trace("createUsers end");
        return userId;
    }

    @Override
    public void addUserProductRoles(String institutionId, String productId, String userId, CreateUserDto user) {
        log.trace("addProductUser start");
        log.debug("addProductUser institutionId = {}, productId = {}, userId = {}, user = {}", institutionId, productId, userId, user);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, A_PRODUCT_ID_IS_REQUIRED);
        Assert.hasText(userId, A_USER_ID_IS_REQUIRED);
        Assert.notNull(user, AN_USER_IS_REQUIRED);

        Product product = productsConnector.getProduct(productId);
        user.getRoles().forEach(role -> {
            EnumMap<PartyRole, ProductRoleInfo> roleMappings = product.getRoleMappings();
            role.setLabel(Product.getLabel(role.getProductRole(), roleMappings).orElse(null));
            Optional<PartyRole> partyRole = Product.getPartyRole(role.getProductRole(), roleMappings, PARTY_ROLE_WHITE_LIST);
            role.setPartyRole(partyRole.orElseThrow(() ->
                    new InvalidProductRoleException(String.format("Product role '%s' is not valid", role.getProductRole()))));
        });

        msCoreConnector.createUsers(institutionId, productId, userId, user, product.getTitle());
        log.trace("addProductUser end");
    }


    @Override
    public OnboardingRequestInfo getOnboardingRequestInfo(String tokenId) {
        log.trace("getOnboardingRequestInfo start");
        log.debug("getOnboardingRequestInfo tokenId = {}", tokenId);
        final OnboardingRequestInfo onboardingRequestInfo = msCoreConnector.getOnboardingRequestInfo(tokenId);
        // In case of PT onboarding, the field manager is empty
        if(Objects.nonNull(onboardingRequestInfo.getInstitutionInfo()) && !InstitutionType.PT.equals(onboardingRequestInfo.getInstitutionInfo().getInstitutionType())) {
            onboardingRequestInfo.getManager().setUser(userRegistryConnector.getUserByInternalId(onboardingRequestInfo.getManager().getId(), USER_FIELD_LIST_ENHANCED));
        }
        onboardingRequestInfo.getAdmins().forEach(userInfo -> userInfo.setUser(userRegistryConnector.getUserByInternalId(userInfo.getId(), USER_FIELD_LIST_ENHANCED)));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getOnboardingRequestInfo result = {}", onboardingRequestInfo);
        log.trace("getOnboardingRequestInfo end");
        return onboardingRequestInfo;
    }

    @Override
    public void approveOnboardingRequest(String tokenId) {
        log.trace("approveOnboardingRequest start");
        log.debug("approveOnboardingRequest tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        msCoreConnector.approveOnboardingRequest(tokenId);
        log.trace("approveOnboardingRequest end");
    }

    @Override
    public void rejectOnboardingRequest(String tokenId) {
        log.trace("rejectOnboardingRequest start");
        log.debug("rejectOnboardingRequest tokenId = {}", tokenId);
        Assert.hasText(tokenId, REQUIRED_TOKEN_ID_MESSAGE);
        msCoreConnector.rejectOnboardingRequest(tokenId);
        log.trace("rejectOnboardingRequest end");
    }

    @Override
    public Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updateInstitutionResource) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, updateInstitutionResource = {}", institutionId, updateInstitutionResource);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(updateInstitutionResource, REQUIRED_UPDATE_RESOURCE_MESSAGE);
        Institution institution = msCoreConnector.updateInstitutionDescription(institutionId, updateInstitutionResource);
        log.debug("updateInstitutionDescription result = {}", institution);
        log.trace("updateInstitutionDescription end");
        return institution;
    }

    @Override
    public Institution findInstitutionById(String institutionId) {
        log.trace("findInstitutionById start");
        log.debug("findInstitutionById institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Institution institution = msCoreConnector.getInstitution(institutionId);
        if (institution != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(SelfCareGrantedAuthority.class::cast)
                    .filter(grantedAuthority -> institutionId.equals(grantedAuthority.getInstitutionId()))
                    .findAny();

            if (selcAuthority.isPresent()) {
                Map<String, ProductGrantedAuthority> userAuthProducts = ((SelfCareGrantedAuthority) selcAuthority.get()).getRoleOnProducts();

                if (LIMITED.name().equals(selcAuthority.get().getAuthority())) {
                    institution.setOnboarding(institution.getOnboarding().stream()
                            .filter(product -> userAuthProducts.containsKey(product.getProductId()))
                            .peek(product -> product.setAuthorized(true))
                            .peek(product -> product.setUserRole(LIMITED.name()))
                            .collect(Collectors.toList()));
                } else {
                    institution.getOnboarding().forEach(product -> {
                        product.setAuthorized(userAuthProducts.containsKey(product.getProductId()));
                        Optional.ofNullable(userAuthProducts.get(product.getProductId()))
                                .ifPresentOrElse(authority -> product.setUserRole(authority.getAuthority()), () -> product.setUserRole(null));
                    });
                }
            }
        }
        log.debug("findInstitutionById result = {}", institution);
        log.trace("findInstitutionById end");
        return institution;
    }

}
