package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@Api(tags = "user")
@RequestMapping(value = "/v2", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserV2Controller {

    private final UserV2Service userService;
    private final InstitutionResourceMapper institutionResourceMapper;

    @PutMapping(value = "/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.updateUserById}")
    public void updateUser(@ApiParam("${swagger.dashboard.user.model.id}")
                           @PathVariable("id")
                                   UUID id,
                           @ApiParam("${swagger.dashboard.institutions.model.id}")
                           @RequestParam(value = "institutionId")
                                   String institutionId,
                           @RequestBody
                           @Valid
                                   UpdateUserDto updateUserDto) {
        log.trace("updateUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "id = {}, institutionId = {}, userDto = {}", id, institutionId, updateUserDto);
        userService.updateUser(id, institutionId, UserMapper.fromUpdateUser(updateUserDto, institutionId));
        log.trace("updateUser end");
    }

    @GetMapping("/institutions")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionBaseResource> getInstitutions(Authentication authentication) {

        log.trace("getInstitutions start");
        String userId = ((SelfCareUser) authentication.getPrincipal()).getId();
        Collection<InstitutionInfo> institutions = userService.getInstitutions(userId);

        List<InstitutionBaseResource> result = institutions.stream()
                .map(institutionResourceMapper::toResource)
                .toList();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");

        return result;
    }
}
