package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.MemberUUID;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

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
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.createUserGroup}")
    public UserGroupResource createUserGroup(@RequestBody
                                             @Valid
                                                     CreateUserGroupDto group) {
        log.trace("createGroup start");
        log.debug("createGroup group = {}", group);
        log.debug("createGroup result = {}", group);
        log.trace("createGroup end");
        return null;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.deleteUserGroup}")
    public void deleteGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                            @PathVariable("id")
                                    String id) {
        log.trace("deteleGroup start");
        log.debug("deleteGroup id = {}", id);
        log.trace("deteleGroup end");

    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.activateUserGroup}")
    public void activateGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                              @PathVariable("id")
                                      String id) {
        log.trace("activateGroup start");
        log.debug("activateGroup id = {}", id);
        log.trace("activateGroup end");
    }

    @PostMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.suspendUserGroup}")
    public void suspendGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                             @PathVariable("id")
                                     String id) {
        log.trace("suspendGroup start");
        log.debug("suspendGroup id = {}", id);
        log.trace("suspendGroup end");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.updateUserGroup}")
    public UserGroupResource updateUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                             @PathVariable("id")
                                                     String id,
                                             @RequestBody
                                             @Valid
                                                     UpdateUserGroupDto groupDto) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup id = {}, groupDto = {}", id, groupDto);
        log.debug("updateUserGroup result = {}", id);
        log.trace("updateUserGroup end");
        return null;
    }

    @PatchMapping(value = "/{id}/members")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.addMember}")
    public void addMemberToUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                     @PathVariable("id")
                                             String id,
                                     @RequestBody
                                     @Valid
                                             MemberUUID member) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup id = {}", id);
        log.trace("addMemberToUserGroup end");
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.getUserGroup}")
    public UserGroupResource getUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                          @PathVariable("id")
                                                  String id) {
        log.trace("getUserGroup start");
        log.debug("getUserGroup id = {}", id);
        UserGroupResource groupResource = null;
        log.debug("getUserGroup result = {}", groupResource);
        log.trace("getUserGroup end");
        return groupResource;
    }

    @GetMapping(value = "/userGroups")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.getGroupsByInstitutionAndProduct}")
    public List<UserGroupResource> getGroupsByInstitutionAndProductIds(@ApiParam("${swagger.dashboard.user-group.model.institutionId}")
                                                                       @RequestParam(value = "institutionId")
                                                                               String institutionId,
                                                                       @ApiParam("${swagger.dashboard.user-group.model.productId}")
                                                                       @RequestParam(value = "productId")
                                                                               String productId,
                                                                       Pageable pageable) {
        log.trace("getGroupsByInstitutionAndProductIds start");
        log.debug("getGroupsByInstitutionAndProductIds institutionId = {}, productId = {}, pageable = {}", institutionId, productId, pageable);
        List<UserGroupResource> result = null;
        log.debug("getGroupsByInstitutionAndProductIds result = {}", result);
        log.trace("getGroupsByInstitutionAndProductIds end");
        return result;
    }

    @DeleteMapping(value = "/{userGroupId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.deleteMember}")
    public void deleteMemberFromUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                          @PathVariable("userGroupId")
                                                  String userGroupId,
                                          @ApiParam("${swagger.dashboard.user-group.model.memberId}")
                                          @PathVariable("memberId")
                                                  UUID memberId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup userGroupId = {}, memberId = {}", userGroupId, memberId);
        log.trace("deleteMemberFromUserGroup end");
    }
}
