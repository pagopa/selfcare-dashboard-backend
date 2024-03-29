package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.*;
import it.pagopa.selfcare.commons.web.model.Page;
import it.pagopa.selfcare.commons.web.model.mapper.PageMapper;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.core.UserGroupService;
import it.pagopa.selfcare.dashboard.web.model.mapper.GroupMapper;
import it.pagopa.selfcare.dashboard.web.model.user_groups.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/v1/user-groups", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = "Conflict")
    })
    public UserGroupIdResource createUserGroup(@RequestBody
                                               @Valid
                                                       CreateUserGroupDto group) {
        log.trace("createGroup start");
        log.debug("createGroup group = {}", group);
        CreateUserGroup userGroup = GroupMapper.fromDto(group);
        String groupId = groupService.createUserGroup(userGroup);
        UserGroupIdResource result = GroupMapper.toIdResource(groupId);
        log.debug("createGroup result = {}", result);
        log.trace("createGroup end");
        return result;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.deleteUserGroup}")
    public void deleteUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                @PathVariable("id")
                                        String id) {
        log.trace("deleteGroup start");
        log.debug("deleteGroup id = {}", id);
        groupService.delete(id);
        log.trace("deleteGroup end");

    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.activateUserGroup}")
    public void activateUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                  @PathVariable("id")
                                          String id) {
        log.trace("activateGroup start");
        log.debug("activateGroup id = {}", id);
        groupService.activate(id);
        log.trace("activateGroup end");
    }

    @PostMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.suspendUserGroup}")
    public void suspendUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                 @PathVariable("id")
                                         String id) {
        log.trace("suspendGroup start");
        log.debug("suspendGroup id = {}", id);
        groupService.suspend(id);
        log.trace("suspendGroup end");
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.updateUserGroup}")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = "Conflict")
    })
    public void updateUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                @PathVariable("id")
                                        String id,
                                @RequestBody
                                @Valid
                                        UpdateUserGroupDto groupDto) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup id = {}, groupDto = {}", id, groupDto);
        groupService.updateUserGroup(id, GroupMapper.fromDto(groupDto));
        log.debug("updateUserGroup result = {}", id);
        log.trace("updateUserGroup end");
    }

    @PostMapping(value = "/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.addMember}")
    public void addMemberToUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                     @PathVariable("id")
                                             String id,
                                     @ApiParam("${swagger.dashboard.user.model.id}")
                                     @PathVariable("userId")
                                             UUID member) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup id = {}", id);
        groupService.addMemberToUserGroup(id, member);
        log.trace("addMemberToUserGroup end");
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.getUserGroup}")
    public UserGroupResource getUserGroupById(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                              @PathVariable("id")
                                                      String id,
                                              @ApiParam("${swagger.dashboard.user-group.model.institutionId}")
                                              @RequestParam(value = "institutionId", required = false)
                                                      Optional<String> institutionId) {
        log.trace("getUserGroup start");
        log.debug("getUserGroup id = {}, institutionId = {}", id, institutionId);
        UserGroupInfo groupInfo = groupService.getUserGroupById(id, institutionId);
        UserGroupResource groupResource = GroupMapper.toResource(groupInfo);
        log.debug("getUserGroup result = {}", groupResource);
        log.trace("getUserGroup end");
        return groupResource;
    }


    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.getUserGroups}")
    public Page<UserGroupPlainResource> getUserGroups(@ApiParam("${swagger.dashboard.user-group.model.institutionId}")
                                                      @RequestParam(value = "institutionId", required = false)
                                                              Optional<String> institutionId,
                                                      @ApiParam("${swagger.dashboard.user-group.model.productId}")
                                                      @RequestParam(value = "productId", required = false)
                                                              Optional<String> productId,
                                                      @ApiParam("${swagger.dashboard.user.model.id}")
                                                      @RequestParam(value = "userId", required = false)
                                                              Optional<UUID> memberId,
                                                      Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, pageable = {}", institutionId, productId, pageable);
        Page<UserGroupPlainResource> groups = PageMapper.map(groupService.getUserGroups(institutionId, productId, memberId, pageable)
                .map(GroupMapper::toPlainGroupResource));
        log.debug("getUserGroups result = {}", groups);
        log.trace("getUserGroups end");
        return groups;
    }

    @DeleteMapping(value = "/{userGroupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.deleteMember}")
    public void deleteMemberFromUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                          @PathVariable("userGroupId")
                                                  String userGroupId,
                                          @ApiParam("${swagger.dashboard.user.model.id}")
                                          @PathVariable("userId")
                                                  UUID memberId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup userGroupId = {}, memberId = {}", userGroupId, memberId);
        groupService.deleteMemberFromUserGroup(userGroupId, memberId);
        log.trace("deleteMemberFromUserGroup end");
    }

}
