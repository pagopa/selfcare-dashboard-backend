package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.core.ProductsService;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/dashboard", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "dashboard")
public class DashboardController {

    private final ProductsService productsService;


    @Autowired
    public DashboardController(ProductsService productsService) {
        this.productsService = productsService;
    }


    @GetMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.products.api.getProducts}")
    public List<ProductsResource> getProducts() {
        List<Product> products = productsService.getProducts();
        return products.stream()
                .map(ProductsMapper::toResource)
                .collect(Collectors.toList());
    }

}
