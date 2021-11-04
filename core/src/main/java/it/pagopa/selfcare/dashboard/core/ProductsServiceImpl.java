package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.client.ProductsRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.products.Product;
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

    private final ProductsRestClient restClient;

    @Autowired
    public ProductsServiceImpl(ProductsRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Product> getProducts() {
        List<Product> products = restClient.getProducts();
        // TODO call get org enabled product
        products = products.stream()// TODO filter org enabled product
                .filter(product -> product.getId().equals(""))
                .collect(Collectors.toList());
        Optional<? extends GrantedAuthority> techAdminAuthority = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .filter(grantedAuthority -> "TECH_ADMIN".equals(grantedAuthority.getAuthority()))
                .findAny();
        if (techAdminAuthority.isPresent()
                // TODO: add && check instance of SelfCareGrantedAuthority
                // TODO: cast to SelfCareGrantedAuthority and check if getProducts is not null
        ) {
            List<String> userAuthProducts = null; // TODO: ((SelfCareGrantedAuthority) techAdminAuthority.get()).getProducts()
            // TODO filter user auth products
            products = products.stream()// TODO filter org enabled product
                    .filter(product -> userAuthProducts.contains(product.getId()))
                    .collect(Collectors.toList());
        }
//        else {
//                 products = products.stream()
//                .filter(product -> product.getId().equals(""))
//                .collect(Collectors.toList());
//        }

        return products;
    }
}
