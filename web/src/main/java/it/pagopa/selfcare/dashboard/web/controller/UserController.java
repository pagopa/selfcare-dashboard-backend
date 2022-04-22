package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.core.UserRegistryService;
import it.pagopa.selfcare.dashboard.web.model.EmbeddedExternalIdDto;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.web.model.user.UserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "user")
public class UserController {

    private final UserRegistryService userRegistryService;

    @Autowired
    public UserController(UserRegistryService userRegistryService) {
        this.userRegistryService = userRegistryService;
    }

    @PostMapping(value = "/search")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.search}")
    public UserResource search(@ApiParam("${swagger.dashboard.user.model.externalId}")
                               @RequestBody
                                       EmbeddedExternalIdDto externalId,
                               @ApiParam("${swagger.dashboard.institutions.model.id}")
                               @RequestParam(value = "institutionId")
                                       String institutionId) {
        log.trace("getUserByExternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId externalId = {}", externalId);
        it.pagopa.selfcare.dashboard.connector.model.user.UserResource user = userRegistryService.search(externalId.getExternalId());
        UserResource result = UserMapper.toUserResource(user, institutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId result = {}", result);
        log.trace("getUserByExternalId end");
        return result;
    }

    @PutMapping(value = "/{id}")
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
        userRegistryService.updateUser(id, institutionId, UserMapper.fromUpdateUser(updateUserDto));
        log.trace("updateUser end");
    }

    @PostMapping(value = "/save-user")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.saveUser}")
    public void saveUser(@RequestBody
                         @Valid
                                 UserDto userDto) {

    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.getUserByInternalId}")
    public UserResource getUserByInternalId(@ApiParam("${swagger.dashboard.user.model.id}")
                                            @PathVariable("id") UUID id,
                                            @ApiParam("${swagger.dashboard.institutions.model.id}")
                                            @RequestParam(value = "institutionId")
                                                    String institutionId) {
        return null;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.deleteUserById}")
    public void deleteUserById(@ApiParam("${swagger.dashboard.user.model.id}")
                               @PathVariable("id") UUID id) {

    }

}
