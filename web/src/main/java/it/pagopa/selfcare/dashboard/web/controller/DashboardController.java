package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;
import it.pagopa.selfcare.dashboard.connector.rest.model.products.Product;
import it.pagopa.selfcare.dashboard.core.PartyManagementService;
import it.pagopa.selfcare.dashboard.core.ProductsService;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;
import it.pagopa.selfcare.dashboard.web.model.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.OrganizationMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@Api(tags = "dashboard")
public class DashboardController {

    private final PartyManagementService partyManagementService;
    private final ProductsService productsService;

    @Autowired
    public DashboardController(PartyManagementService partyManagementService, ProductsService productsService) {
        this.partyManagementService = partyManagementService;
        this.productsService = productsService;
    }

    @GetMapping("/organization/{organizationId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.api.getOrganization}")
    public OrganizationResource getOrganization(@ApiParam("${swagger.dashboard.model.id}") @PathVariable("organizationId") String organizationId) {

        Organization organization = partyManagementService.getOrganization(organizationId);
        return OrganizationMapper.toResource(organization);
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
