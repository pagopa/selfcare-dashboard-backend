package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient(name = "${rest-client.products.serviceCode}", url = "${rest-client.products.base-url}")
public interface ProductsRestClient extends ProductsConnector {

    @GetMapping(value = "${rest-client.products.getProducts.path}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<Product> getProducts();
}
