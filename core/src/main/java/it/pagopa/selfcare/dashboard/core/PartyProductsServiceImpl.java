package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.client.ProductsRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.products.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
class PartyProductsServiceImpl implements ProductsService {

    private final ProductsRestClient restClient;

    @Autowired
    public PartyProductsServiceImpl(ProductsRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = restClient.getProducts();
        // TODO call get org enabled product
//        products = products.stream()// TODO filter org enabled product
//                .filter(product -> product.getId().equals(""))
//                .collect(Collectors.toList());
        boolean isTechRef = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> "TECH_ADMIN".equals(grantedAuthority.getAuthority()));
        if (isTechRef) {
            // TODO filter user auth products
        }

        return products;
    }
}
