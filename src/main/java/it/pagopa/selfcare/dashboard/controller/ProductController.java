package it.pagopa.selfcare.dashboard.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.dashboard.model.backoffice.BrokerInfo;

import it.pagopa.selfcare.dashboard.service.BrokerService;
import it.pagopa.selfcare.dashboard.service.ProductService;
import it.pagopa.selfcare.dashboard.model.mapper.BrokerResourceMapper;
import it.pagopa.selfcare.dashboard.model.mapper.ProductsMapper;
import it.pagopa.selfcare.dashboard.model.product.BrokerResource;
import it.pagopa.selfcare.dashboard.model.product.ProductRoleMappingsResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "products")
public class ProductController {

    private final ProductService productService;
    private final BrokerService brokerService;
    private final BrokerResourceMapper brokerResourceMapper;
    private static final String PAGO_PA_PRODUCT_ID = "prod-pagopa";

    @Autowired
    public ProductController(ProductService productService,
                             BrokerService brokerService,
                             BrokerResourceMapper brokerResourceMapper) {
        this.productService = productService;
        this.brokerService = brokerService;
        this.brokerResourceMapper = brokerResourceMapper;
    }

    @GetMapping(value = "/{productId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.getProductRoles}")
    public Collection<ProductRoleMappingsResource> getProductRoles(@ApiParam("${swagger.dashboard.products.model.id}")
                                                                   @PathVariable("productId") String productId,
                                                                  @ApiParam("${swagger.dashboard.institutions.model.institutionType}")
                                                                  @RequestParam(name = "institutionType", required = false) String institutionType) {
        log.trace("getProductRoles start");
        log.debug("productId = {}", productId);
        Collection<ProductRoleMappingsResource> result = ProductsMapper.toProductRoleMappingsResource(productService.getProductRoles(productId, institutionType));
        log.debug("getProductRoles result = {}", result);
        log.trace("getProductRoles end");

        return result;
    }

    @GetMapping(value = "/{productId}/brokers/{institutionType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.product.api.getProductBrokers}")
    public Collection<BrokerResource> getProductBrokers(@ApiParam("${swagger.dashboard.products.model.id}")
                                                        @PathVariable("productId")
                                                        String productId,
                                                        @ApiParam("${swagger.dashboard.products.model.institutionType}")
                                                        @PathVariable("institutionType")
                                                        String institutionType) {
        log.trace("getProductBrokers start");
        log.debug("productId = {}, institutionType = {}", productId, institutionType);

        List<BrokerInfo> brokers = PAGO_PA_PRODUCT_ID.equals(productId)
            ?  brokerService.findAllByInstitutionType(InstitutionType.valueOf(institutionType).name())
            : brokerService.findInstitutionsByProductAndType(productId, institutionType);

        Collection<BrokerResource>  result = brokerResourceMapper.toResourceList(brokers);
        log.debug("getProductBrokers result = {}", result);
        log.trace("getProductBrokers end");
        return result;
    }

}
