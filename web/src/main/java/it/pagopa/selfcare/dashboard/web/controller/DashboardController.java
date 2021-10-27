package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;
import it.pagopa.selfcare.dashboard.core.PartyManagementService;
import it.pagopa.selfcare.dashboard.web.model.OrganizationResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.OrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@Api(tags = "dashboard")
public class DashboardController {

    private final PartyManagementService partyManagementService;

    @Autowired
    public DashboardController(PartyManagementService partyManagementService) {
        this.partyManagementService = partyManagementService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.api.getOrganization}")
    public OrganizationResource getOrganization(@ApiParam("${swagger.dashboard.model.id}") @PathVariable("id") String id) {

        Organization organization = partyManagementService.getOrganization(id);
        return OrganizationMapper.toResource(organization);
    }
}
