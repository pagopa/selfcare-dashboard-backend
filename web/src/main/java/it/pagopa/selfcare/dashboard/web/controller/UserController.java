package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.TargetEnvironment;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.core.UserRegistryService;
import it.pagopa.selfcare.dashboard.web.model.EmbeddedExternalIdDto;
import it.pagopa.selfcare.dashboard.web.model.UserResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/external-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.getUserByExternalId}")
    public UserResource getUserByExternalId(@ApiParam()
                                            @RequestBody
                                                    EmbeddedExternalIdDto externalId) {
        log.trace("getUserByExternalId start");
        if (!TargetEnvironment.PROD.equals(TargetEnvironment.getCurrent())) {
            log.debug("getUserByExternalId externalId = {}", externalId);
        }
        User user = userRegistryService.getUser(externalId.getExternalId());
        UserResource result = UserMapper.toUserResource(user);
        if (!TargetEnvironment.PROD.equals(TargetEnvironment.getCurrent())) {
            log.debug("getUserByExternalId result = {}", result);
        }
        log.trace("getUserByExternalId end");
        return result;
    }
}
