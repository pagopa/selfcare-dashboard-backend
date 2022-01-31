package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    private static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private static final Set<String> PARTY_ROLE_WHITE_LIST = Set.of("SUB_DELEGATE", "OPERATOR");

    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;


    @Autowired
    public InstitutionServiceImpl(PartyConnector partyConnector, ProductsConnector productsConnector) {
        this.partyConnector = partyConnector;
        this.productsConnector = productsConnector;
    }


    @Override
    public InstitutionInfo getInstitution(String institutionId) {
        log.trace("InstitutionServiceImpl.getInstitution start");
        log.debug("InstitutionServiceImpl.getInstitution institutionId = {}", institutionId);
        InstitutionInfo result = partyConnector.getInstitution(institutionId);
        log.debug("InstitutionServiceImpl.getInstitution result = {}", result);
        log.trace("InstitutionServiceImpl.getInstitution end");
        return result;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("InstitutionServiceImpl.getInstitutions start");
        Collection<InstitutionInfo> result = partyConnector.getInstitutions();
        log.debug("InstitutionServiceImpl.getInstitutions result = {}", result);
        log.trace("InstitutionServiceImpl.getInstitutions end");
        return result;
    }

    @Override
    public List<Product> getInstitutionProducts(String institutionId) {
        log.trace("InstitutionServiceImpl.getInstitutionProducts start");
        log.debug("InstitutionServiceImpl.getInstitutionProducts institutionId = {}", institutionId);
        List<Product> products = productsConnector.getProducts();

        if (!products.isEmpty()) {
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
                    products = products.stream()
                            .filter(product -> institutionsProductsMap.containsKey(product.getId()))
                            .filter(product -> userAuthProducts.containsKey(product.getId()))
                            .peek(product -> product.setAuthorized(true))
                            .peek(product -> product.setUserRole(LIMITED.name()))
                            .peek(product -> product.setStatus(institutionsProductsMap.get(product.getId()).getStatus()))
                            .collect(Collectors.toList());

                } else {
                    products.forEach(product -> {
                        product.setAuthorized(userAuthProducts.containsKey(product.getId()));
                        product.setStatus(Optional.ofNullable(institutionsProductsMap.get(product.getId()))
                                .map(PartyProduct::getStatus)
                                .orElse(ProductStatus.INACTIVE));
                        Optional.ofNullable(userAuthProducts.get(product.getId()))
                                .ifPresentOrElse(authority -> product.setUserRole(authority.getAuthority()), () -> product.setUserRole(null));
                    });
                }
            }
        }

        log.debug("InstitutionServiceImpl.getInstitutionProducts result = {}", products);
        log.trace("InstitutionServiceImpl.getInstitutionProducts end");
        return products;
    }


    @Override
    public Collection<UserInfo> getInstitutionUsers(String institutionId, Optional<String> productId, Optional<SelfCareAuthority> role) {
        log.trace("InstitutionServiceImpl.getInstitutionUsers start");
        log.debug("InstitutionServiceImpl.getInstitutionUsers institutionId = {}, productId = {}, role = {}", institutionId, productId, role);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(productId, "An Optional Product id object is required");
        Assert.notNull(role, "An Optional role object is required");

        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        Map<String, Product> idToProductMap = productsConnector.getProducts().stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        userInfos.forEach(userInfo ->
                userInfo.getProducts().forEach(productInfo ->
                        productInfo.setTitle(idToProductMap.get(productInfo.getId()).getTitle())));

        log.debug("InstitutionServiceImpl.getInstitutionUsers result = {}", userInfos);
        log.trace("InstitutionServiceImpl.getInstitutionUsers end");
        return userInfos;
    }


    @Override
    public Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<SelfCareAuthority> role) {
        log.trace("InstitutionServiceImpl.getInstitutionProductUsers start");
        log.debug("InstitutionServiceImpl.getInstitutionProductUsers institutionId = {}, productId = {}, role = {}", institutionId, productId, role);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, "A Product id is required");
        Assert.notNull(role, "An Optional role object is required");

        Collection<UserInfo> result = partyConnector.getUsers(institutionId, role, Optional.of(productId));

        log.debug("InstitutionServiceImpl.getInstitutionProductUsers result = {}", result);
        log.trace("InstitutionServiceImpl.getInstitutionProductUsers end");
        return result;
    }


    @Override
    public void createUsers(String institutionId, String productId, CreateUserDto user) {
        log.trace("InstitutionServiceImpl.createUsers start");
        log.debug("InstitutionServiceImpl.createUsers institutionId = {}, productId = {}, user = {}", institutionId, productId, user);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, "A Product id is required");
        Assert.notNull(user, "An User is required");

        Map<String, List<String>> productRoleMappings = productsConnector.getProductRoleMappings(productId);
        Optional<String> partyRole = productRoleMappings.entrySet().stream()
                .filter(entry -> PARTY_ROLE_WHITE_LIST.contains(entry.getKey()))
                .filter(entry -> entry.getValue().contains(user.getProductRole()))
                .map(Map.Entry::getKey)
                .findAny();
        user.setPartyRole(partyRole.orElseThrow(() ->
                new InvalidProductRoleException(String.format("Product role '%s' is not valid", user.getProductRole()))));

        partyConnector.createUsers(institutionId, productId, user);

        log.trace("InstitutionServiceImpl.createUsers end");
    }
}
