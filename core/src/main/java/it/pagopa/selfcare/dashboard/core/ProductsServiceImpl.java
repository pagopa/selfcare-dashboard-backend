package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.Authority.LIMITED;

@Service
class ProductsServiceImpl implements ProductsService {

    private final ProductsConnector productsConnector;
    private final PartyConnector partyConnector;


    @Autowired
    public ProductsServiceImpl(ProductsConnector productsConnector, PartyConnector partyConnector) {
        this.productsConnector = productsConnector;
        this.partyConnector = partyConnector;
    }


    @Override
    public List<Product> getProducts() {//TODO: add InstitutionId param
        List<Product> products = productsConnector.getProducts();

        if (!products.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .findAny();

            if (selcAuthority.isPresent()) {
                String institutionId = ((SelfCareAuthenticationDetails) authentication.getDetails())
                        .getInstitutionId();
                List<String> institutionsProducts = partyConnector.getInstitutionProducts(institutionId);
                Collection<ProductGrantedAuthority> userAuthProducts = ((SelfCareGrantedAuthority) selcAuthority.get()).getRoleOnProducts();

                if (LIMITED.name().equals(selcAuthority.get().getAuthority())) {
                    products = products.stream()
                            .filter(product -> institutionsProducts.contains(product.getCode()))
                            .filter(product -> userAuthProducts.stream().anyMatch(productRole -> productRole.getProductCode().equals(product.getCode())))//FIXME:use getId
                            .peek(product -> product.setActive(true))
                            .peek(product -> product.setAuthorized(true))
                            .collect(Collectors.toList());

                } else {
                    products.forEach(product -> {
                        product.setAuthorized(userAuthProducts.stream().anyMatch(productRole -> productRole.getProductCode().equals(product.getCode())));//FIXME:use getId
                        product.setActive(institutionsProducts.contains(product.getCode()));
                    });
                }
            }
        }

        return products;
    }
}
