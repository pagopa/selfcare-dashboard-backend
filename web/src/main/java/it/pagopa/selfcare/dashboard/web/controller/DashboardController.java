package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.core.ProductsService;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.OrganizationMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/dashboard", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "dashboard")
public class DashboardController {

    private final PartyConnector partyConnector;
    private final ProductsService productsService;


    @Autowired
    public DashboardController(PartyConnector partyConnector, ProductsService productsService) {
        this.partyConnector = partyConnector;
        this.productsService = productsService;
    }


    @GetMapping("/organization/{organizationId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.api.getOrganization}")
    public OrganizationResource getOrganization(@ApiParam("${swagger.dashboard.model.id}")
                                                @PathVariable("organizationId")
                                                        String organizationId) {
        OnBoardingInfo onBoardingInfo = partyConnector.getOnBoardingInfo(organizationId);
        return OrganizationMapper.toResource(onBoardingInfo);
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
