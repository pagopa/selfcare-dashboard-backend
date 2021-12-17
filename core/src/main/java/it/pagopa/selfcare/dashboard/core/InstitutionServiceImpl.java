package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
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
        return partyConnector.getInstitution(institutionId);
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        return partyConnector.getInstitutions();
    }

    @Override
    public List<Product> getInstitutionProducts(String institutionId) {
        List<Product> products = productsConnector.getProducts();

        if (!products.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .findAny();

            if (selcAuthority.isPresent()) {
                Map<String, ProductGrantedAuthority> userAuthProducts = ((SelfCareGrantedAuthority) selcAuthority.get()).getRoleOnProducts();
                List<String> institutionsProducts = partyConnector.getInstitutionProducts(institutionId);

                if (LIMITED.name().equals(selcAuthority.get().getAuthority())) {
                    products = products.stream()
                            .filter(product -> institutionsProducts.contains(product.getId()))
                            .filter(product -> userAuthProducts.containsKey(product.getId()))
                            .peek(product -> product.setActive(true))
                            .peek(product -> product.setAuthorized(true))
                            .peek(product -> product.setUserRole(LIMITED.name()))
                            .collect(Collectors.toList());

                } else {
                    products.forEach(product -> {
                        product.setAuthorized(userAuthProducts.containsKey(product.getId()));
                        product.setActive(institutionsProducts.contains(product.getId()));
                        Optional.ofNullable(userAuthProducts.get(product.getId()))
                                .ifPresentOrElse(authority -> product.setUserRole(authority.getAuthority()), () -> product.setUserRole(null));
                    });
                }
            }
        }

        return products;
    }


    @Override
    public Collection<UserInfo> getInstitutionUsers(String institutionId, Optional<String> productId, Optional<SelfCareAuthority> role) {
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(productId, "An Optional Product id object is required");
        Assert.notNull(role, "An Optional role object is required");

        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        Map<String, Product> idToProductMap = productsConnector.getProducts().stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        userInfos.forEach(userInfo ->
                userInfo.getProducts().forEach(productInfo ->
                        productInfo.setTitle(idToProductMap.get(productInfo.getId()).getTitle())));

        return userInfos;
    }


    @Override
    public Collection<UserInfo> getInstitutionProductUsers(String institutionId, String productId, Optional<SelfCareAuthority> role) {
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(productId, "A Product id is required");
        Assert.notNull(role, "An Optional role object is required");

        return partyConnector.getUsers(institutionId, role, Optional.of(productId));
    }


    @Override
    public void createUsers(String institutionId, String productId, CreateUserDto user) {
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
    }
}