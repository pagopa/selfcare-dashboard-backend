package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionV2Service;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping(value = "/v2/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionV2Controller {

    private final InstitutionV2Service institutionV2Service;


    @Autowired
    public InstitutionV2Controller(InstitutionV2Service institutionV2Service) {
        this.institutionV2Service = institutionV2Service;
    }

    @GetMapping(value = "/{institutionId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionUser}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public InstitutionUserDetailsResource getInstitutionUser(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                             @PathVariable("institutionId")
                                                             String institutionId,
                                                             @ApiParam("${swagger.dashboard.user.model.id}")
                                                             @PathVariable("userId")
                                                             String userId,
                                                             Authentication authentication) {

        log.trace("getInstitutionUser start");
        String loggedUserId = ((SelfCareUser) authentication.getPrincipal()).getId();

        log.debug("getInstitutionUser institutionId = {}, userId = {}", institutionId, userId);
        UserInfo userInfo = institutionV2Service.getInstitutionUser(institutionId, userId, loggedUserId);
        InstitutionUserDetailsResource result = UserMapper.toInstitutionUserDetails(userInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionUser result = {}", result);
        log.trace("getInstitutionUser end");
        return result;
    }
}
