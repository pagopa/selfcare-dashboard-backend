package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class ProductsServiceImpl implements ProductsService {

    private final ProductsConnector productsConnector;
    @Autowired
    private PartyConnector partyConnector;

    @Autowired
    public ProductsServiceImpl(ProductsConnector productsConnector) {
        this.productsConnector = productsConnector;
    }

    @Override
    public List<Product> getProducts() {
//        String institutionId = ((SelfCareAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails())
//                .getInstitutionId();
//        partyConnector.getOnBoardingInfo(institutionId)
//                .getInstitutions()
//                .stream()
//                .map(InstitutionInfo::getAttributes)//FIXME: replace with org enabled product list
//                .
        List<Product> products = productsConnector.getProducts();
        // TODO call get org enabled product (endpoint TBD)
        // TODO filter org enabled product
//        products = products.stream()
//                .filter(product -> product.getId().equals(""))
//                .collect(Collectors.toList());
        Optional<? extends GrantedAuthority> techAdminAuthority = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(grantedAuthority -> "TECH_REF".equals(grantedAuthority.getAuthority())) //TODO: move role name into Utils class
                .findAny();
        if (techAdminAuthority.isPresent()
                && SelfCareGrantedAuthority.class.isAssignableFrom(techAdminAuthority.get().getClass())
        ) {
            Collection<String> userAuthProducts = ((SelfCareGrantedAuthority) techAdminAuthority.get()).getProducts();
            if (userAuthProducts != null) {
                // TODO filter user auth products
                products = products.stream()// TODO filter org enabled product
                        .filter(product -> userAuthProducts.contains(product.getId()))
                        .collect(Collectors.toList());
            }
        }

        //TODO: add active flag to products

        return products;
    }
}
