package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.core.UserService;
import it.pagopa.selfcare.dashboard.web.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.web.model.user.UserDto;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
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

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/search")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.search}")
    @ApiResponse(responseCode = "404",
            description = "Not Found",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    public UserResource search(@ApiParam("${swagger.dashboard.user.model.searchUserDto}")
                               @RequestBody
                               @Valid
                                       SearchUserDto searchUserDto,
                               @ApiParam("${swagger.dashboard.institutions.model.id}")
                               @RequestParam(value = "institutionId")
                                       String institutionId) {
        log.trace("getUserByExternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId searchUserDto = {}", searchUserDto);
        User user = userService.search(searchUserDto.getFiscalCode());
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
        userService.updateUser(id, institutionId, UserMapper.fromUpdateUser(updateUserDto, institutionId));
        log.trace("updateUser end");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.saveUser}")
    public UserIdResource saveUser(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                   @RequestParam(value = "institutionId")
                                           String institutionId,
                                   @RequestBody
                                   @Valid
                                           UserDto userDto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser userDto = {}", userDto);
        UserId id = userService.saveUser(institutionId, UserMapper.map(userDto, institutionId));
        UserIdResource result = UserMapper.toIdResource(id);
        log.debug("saveUser result = {}", result);
        log.trace("saveUser end");
        return result;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.getUserByInternalId}")
    public UserResource getUserByInternalId(@ApiParam("${swagger.dashboard.user.model.id}")
                                            @PathVariable("id") UUID id,
                                            @ApiParam("${swagger.dashboard.institutions.model.id}")
                                            @RequestParam(value = "institutionId")
                                                    String institutionId) {
        log.trace("getUserByInternalId start");
        log.debug("getUserByInternalId id = {}, institutionId = {}", id, institutionId);
        User user = userService.getUserByInternalId(id);
        UserResource result = UserMapper.toUserResource(user, institutionId);
        log.debug("getUserByInternalId result = {}", result);
        log.trace("getUserByInternalId end");
        return result;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.deleteUserById}")
    public void deleteUserById(@ApiParam("${swagger.dashboard.user.model.id}")
                               @PathVariable("id") UUID id) {

        log.trace("deleteUserById start");
        log.debug("deleteUserById id = {}", id);
        userService.deleteById(id.toString());
        log.trace("deleteUserById end");
    }

}
