package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.model.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "products")
public class ProductController {

    private final ProductService productService;


    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping(value = "/{productId}/roles")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.getProductRoles}")
    public Collection<ProductRoleMappingsResource> getProductRoles(@ApiParam("${swagger.dashboard.products.model.id}")
                                                                   @PathVariable("productId")
                                                                           String productId) {
        log.trace("getProductRoles start");
        log.debug("productId = {}", productId);
        Collection<ProductRoleMappingsResource> result = ProductsMapper.toProductRoleMappingsResource(productService.getProductRoles(productId));
        log.debug("getProductRoles result = {}", result);
        log.trace("getProductRoles start");

        return result;
    }

}
