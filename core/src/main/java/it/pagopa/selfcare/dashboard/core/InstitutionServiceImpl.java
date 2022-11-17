package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.*;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    private static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
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

    private final Optional<EnumSet<RelationshipState>> allowedStates;
    private final UserRegistryConnector userRegistryConnector;
    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;
    private final NotificationService notificationService;


    @Autowired
    public InstitutionServiceImpl(@Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates,
                                  UserRegistryConnector userRegistryConnector, PartyConnector partyConnector,
                                  ProductsConnector productsConnector,
                                  NotificationService notificationService) {
        this.allowedStates = allowedStates == null || allowedStates.length == 0
                ? Optional.empty()
                : Optional.of(EnumSet.copyOf(Arrays.stream(allowedStates)
                .map(RelationshipState::valueOf)
                .collect(Collectors.toList())));
        this.userRegistryConnector = userRegistryConnector;
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
        this.notificationService = notificationService;
    }


    @Override
    public InstitutionInfo getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        InstitutionInfo result = partyConnector.getOnBoardedInstitution(institutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = partyConnector.getOnBoardedInstitutions();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public List<ProductTree> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);
        List<ProductTree> productTrees = productsConnector.getProductsTree();
        if (!productTrees.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(SelfCareGrantedAuthority.class::cast)
                    .filter(grantedAuthority -> institutionId.equals(grantedAuthority.getInstitutionId()))
                    .findAny();

            if (selcAuthority.isPresent()) {
                Map<String, ProductGrantedAuthority> userAuthProducts = ((SelfCareGrantedAuthority) selcAuthority.get()).getRoleOnProducts();
                Map<String, PartyProduct> institutionsProductsMap = partyConnector.getInstitutionProducts(institutionId).stream()
                        .collect(Collectors.toMap(PartyProduct::getId, Function.identity()));

                if (LIMITED.name().equals(selcAuthority.get().getAuthority())) {
                    productTrees = productTrees.stream()
                            .filter(product -> institutionsProductsMap.containsKey(product.getNode().getId()))
                            .filter(product -> userAuthProducts.containsKey(product.getNode().getId()))
                            .peek(product -> product.getNode().setAuthorized(true))
                            .peek(product -> product.getNode().setUserRole(LIMITED.name()))
                            .peek(product -> product.getNode().setOnBoardingStatus(institutionsProductsMap.get(product.getNode().getId()).getOnBoardingStatus()))
                            .collect(Collectors.toList());
                    productTrees.stream()
                            .filter(productTree -> productTree.getChildren() != null)
                            .forEach(productTree -> productTree.setChildren(productTree.getChildren().stream()
                                    .filter(product -> institutionsProductsMap.containsKey(product.getId()))
                                    .filter(product -> userAuthProducts.containsKey(product.getId()))
                                    .peek(product -> product.setAuthorized(true))
                                    .peek(product -> product.setUserRole(LIMITED.name()))
                                    .peek(product -> product.setOnBoardingStatus(institutionsProductsMap.get(product.getId()).getOnBoardingStatus()))
                                    .collect(Collectors.toList())));
                } else {
                    productTrees.forEach(product -> {
                        product.getNode().setAuthorized(userAuthProducts.containsKey(product.getNode().getId()));
                        product.getNode().setOnBoardingStatus(Optional.ofNullable(institutionsProductsMap.get(product.getNode().getId()))
                                .map(PartyProduct::getOnBoardingStatus)
                                .orElse(ProductOnBoardingStatus.INACTIVE));
                        Optional.ofNullable(userAuthProducts.get(product.getNode().getId()))
                                .ifPresentOrElse(authority -> product.getNode().setUserRole(authority.getAuthority()), () -> product.getNode().setUserRole(null));
                    });
                    productTrees.stream()
                            .map(ProductTree::getChildren)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .forEach(product ->
                                    product.setOnBoardingStatus(Optional.ofNullable(institutionsProductsMap.get(product.getId()))
                                            .map(PartyProduct::getOnBoardingStatus)
                                            .orElse(ProductOnBoardingStatus.INACTIVE)));
                }
            }
        }
        log.debug("getInstitutionProducts result = {}", productTrees);
        log.trace("getInstitutionProducts end");
        return productTrees;
    }


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
        userInfoFilter.setRole(role);
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles);
        userInfoFilter.setAllowedState(allowedStates);
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


        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
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
                    throw new IllegalArgumentException(String.format("No matching product found with id %s", key));
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
        userInfoFilter.setUserId(Optional.of(userId));
        userInfoFilter.setAllowedState(allowedStates);

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
        userInfoFilter.setRole(role);
        userInfoFilter.setProductId(Optional.of(productId));
        userInfoFilter.setProductRoles(productRoles);
        userInfoFilter.setAllowedState(allowedStates);
        Collection<UserInfo> result = partyConnector.getUsers(institutionId, userInfoFilter);
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
        partyConnector.createUsers(institutionId, productId, userId.getId().toString(), user);
        notificationService.sendCreatedUserNotification(institutionId, product.getTitle(), user.getEmail(), user.getRoles());
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

        partyConnector.createUsers(institutionId, productId, userId, user);
        notificationService.sendAddedProductRoleNotification(institutionId, product.getTitle(), userId, user.getRoles());
        log.trace("addProductUser end");
    }

}
