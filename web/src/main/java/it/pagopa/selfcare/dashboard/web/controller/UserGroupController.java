package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/user-groups", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "user-groups")
public class UserGroupController {

    private final UserGroupService groupService;

    @Autowired
    public UserGroupController(UserGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping(value = "/")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.groups.api.createUserGroup}")
    public UserGroupResource createUserGroup() {

        return null;
    }


}
