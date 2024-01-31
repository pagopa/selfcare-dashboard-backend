package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
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
@RequestMapping(value = "/support", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "support")
public class SupportController {

    private final SupportService supportService;
    private final SupportMapper supportMapper;

    public SupportController(SupportService supportService,
                             SupportMapper supportMapper) {
        this.supportService = supportService;
        this.supportMapper = supportMapper;
    }

    @Tag(name = "external-v2")
    @PostMapping(produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.support.api.sendRequest}")
    public String sendSupportRequest(@RequestBody @Valid SupportRequestDto supportRequestDto,
                                              Authentication authentication) {
        log.trace("sendSupportRequest start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "sendSupportRequest request = {}", supportRequestDto);
        final SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        String url = supportService.sendRequest(supportMapper.toZendeskRequest(supportRequestDto, selfCareUser));
        log.debug("sendSupportRequest result = {}", url);
        log.trace("sendSupportRequest end");
        return url;
    }
}
