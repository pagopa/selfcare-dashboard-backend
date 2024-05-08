package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportResponse;
import it.pagopa.selfcare.dashboard.core.SupportService;
import it.pagopa.selfcare.dashboard.web.model.mapper.SupportMapper;
import it.pagopa.selfcare.dashboard.web.model.support.SupportRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/v1/support", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "support")
public class SupportController {

    private final SupportService supportService;
    private final SupportMapper supportMapper;

    public SupportController(SupportService supportService,
                             SupportMapper supportMapper) {
        this.supportService = supportService;
        this.supportMapper = supportMapper;
    }

    @Tags({@Tag(name = "external-v2"), @Tag(name = "support")})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.support.api.sendRequest}")
    public SupportResponse sendSupportRequest(@RequestBody @Valid SupportRequestDto supportRequestDto,
                                     Authentication authentication) {
        log.trace("sendSupportRequest start");
        final SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        final SupportResponse supportResponse = supportService.sendRequest(supportMapper.toZendeskRequest(supportRequestDto, selfCareUser));
        log.debug("sendSupportRequest result = {}", supportResponse.toString());
        log.trace("sendSupportRequest end");
        return supportResponse;
    }

}
