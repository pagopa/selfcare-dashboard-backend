package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
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
    public Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role) {
        Collection<UserInfo> userInfos = getUsers(institutionId, role, null);
        Map<String, Product> idToProductMap = productsConnector.getProducts().stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        userInfos.forEach(userInfo ->
                userInfo.getProducts().forEach(productInfo ->
                        productInfo.setTitle(idToProductMap.get(productInfo.getId()).getTitle())));

        return userInfos;
    }


    @Override
    public Collection<UserInfo> getUsers(String institutionId, Optional<SelfCareAuthority> role, Set<String> productIds) {
        Assert.hasText(institutionId, "An Institution id is required");
        Assert.notNull(role, "An Optional role object is required");

        return partyConnector.getUsers(institutionId, role, Optional.ofNullable(productIds));
    }

}
