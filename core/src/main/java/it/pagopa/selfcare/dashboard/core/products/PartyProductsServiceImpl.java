package it.pagopa.selfcare.dashboard.core.products;

import it.pagopa.selfcare.dashboard.connector.rest.client.ProductsRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.products.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartyProductsServiceImpl implements ProductsService {

    private final ProductsRestClient restClient;

    @Autowired
    public PartyProductsServiceImpl(ProductsRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Product> getProducts() {
        return restClient.getProducts();
    }
}
