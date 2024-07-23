package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationIdResource;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.DelegationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/v1/delegations", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "delegations")
public class DelegationController {

    private final DelegationService delegationService;
    private final DelegationMapper delegationMapper;

    public DelegationController(DelegationService delegationService,
                                DelegationMapper delegationMapper) {
        this.delegationService = delegationService;
        this.delegationMapper = delegationMapper;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.delegation.api.createDelegation}")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = "Conflict")
    })
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#delegationRequest.getFrom(), #delegationRequest.getProductId(), null), 'CREATE_DELEGATION')")
    public DelegationIdResource createDelegation(@RequestBody @Valid DelegationRequestDto delegationRequest) {
        log.trace("createDelegation start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createDelegation request = {}", delegationRequest);
        DelegationId delegationId = delegationService.createDelegation(delegationMapper.toDelegation(delegationRequest));
        DelegationIdResource result = delegationMapper.toIdResource(delegationId);
        log.debug("createDelegation result = {}", result);
        log.trace("createDelegation end");
        return result;
    }
}

