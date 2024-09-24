package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.*;
import it.pagopa.selfcare.commons.web.model.Page;
import it.pagopa.selfcare.commons.web.model.mapper.PageMapper;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.core.UserGroupV2Service;
import it.pagopa.selfcare.dashboard.web.model.mapper.GroupMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.GroupMapperV2;
import it.pagopa.selfcare.dashboard.web.model.user_groups.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/v2/user-groups", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Api(tags = "user-groups")
public class UserGroupV2Controller {

    private final UserGroupV2Service groupService;
    private final GroupMapperV2 groupMapperV2;

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.createUserGroup}")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = "Conflict")
    })
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#group.getInstitutionId(), #group.getProductId(), null), 'Selc:ManageProductGroups')")
    public UserGroupIdResource createUserGroup(@RequestBody @Valid CreateUserGroupDto group) {
        log.trace("createGroup start");
        log.debug("createGroup group = {}", group);
        CreateUserGroup userGroup = groupMapperV2.fromDto(group);
        String groupId = groupService.createUserGroup(userGroup);
        UserGroupIdResource result = GroupMapper.toIdResource(groupId);
        log.debug("createGroup result = {}", result);
        log.trace("createGroup end");
        return result;
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.deleteUserGroup}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #id), 'Selc:ManageProductGroups')")
    public void deleteUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                @PathVariable("id") String id) {
        log.trace("deleteGroup start");
        log.debug("deleteGroup id = {}", id);
        groupService.delete(id);
        log.trace("deleteGroup end");

    }

    @PostMapping(value = "/{id}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.activateUserGroup}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #id), 'Selc:ManageProductGroups')")
    public void activateUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                  @PathVariable("id") String id) {
        log.trace("activateGroup start");
        log.debug("activateGroup id = {}", id);
        groupService.activate(id);
        log.trace("activateGroup end");
    }

    @PostMapping(value = "/{id}/suspend", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.suspendUserGroup}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #id), 'Selc:ManageProductGroups')")
    public void suspendUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                 @PathVariable("id") String id) {
        log.trace("suspendGroup start");
        log.debug("suspendGroup id = {}", id);
        groupService.suspend(id);
        log.trace("suspendGroup end");
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.updateUserGroup}")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = "Conflict")
    })
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #id), 'Selc:ManageProductGroups')")
    public void updateUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                @PathVariable("id") String id,
                                @RequestBody @Valid UpdateUserGroupDto groupDto) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup id = {}, groupDto = {}", id, groupDto);
        groupService.updateUserGroup(id, groupMapperV2.fromDto(groupDto));
        log.debug("updateUserGroup result = {}", id);
        log.trace("updateUserGroup end");
    }

    @PostMapping(value = "/{id}/members/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.addMember}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #id), 'Selc:ManageProductGroups')")
    public void addMemberToUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                     @PathVariable("id") String id,
                                     @ApiParam("${swagger.dashboard.user.model.id}")
                                     @PathVariable("userId") UUID member) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup id = {}", id);
        groupService.addMemberToUserGroup(id, member);
        log.trace("addMemberToUserGroup end");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.getUserGroup}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #id), 'Selc:ManageProductGroups')")
    public UserGroupResource getUserGroupById(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                              @PathVariable("id") String id,
                                              @ApiParam("${swagger.dashboard.user-group.model.institutionId}")
                                              @RequestParam(value = "institutionId", required = false) String institutionId) {
        log.trace("getUserGroup start");
        log.debug("getUserGroup id = {}, institutionId = {}", id, institutionId);
        UserGroupInfo groupInfo = groupService.getUserGroupById(id, institutionId);
        UserGroupResource groupResource = GroupMapper.toResource(groupInfo);
        log.debug("getUserGroup result = {}", groupResource);
        log.trace("getUserGroup end");
        return groupResource;
    }


    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.getUserGroups}")
    public Page<UserGroupPlainResource> getUserGroups(@ApiParam("${swagger.dashboard.user-group.model.institutionId}")
                                                      @RequestParam(value = "institutionId", required = false) String institutionId,
                                                      @ApiParam("${swagger.dashboard.user-group.model.productId}")
                                                      @RequestParam(value = "productId", required = false) String productId,
                                                      @ApiParam("${swagger.dashboard.user.model.id}")
                                                      @RequestParam(value = "userId", required = false) UUID memberId,
                                                      Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, pageable = {}", institutionId, productId, pageable);
        Page<UserGroupPlainResource> groups = PageMapper.map(groupService.getUserGroups(institutionId, productId, memberId, pageable)
                .map(groupMapperV2::toPlainUserGroupResource));
        log.debug("getUserGroups result = {}", groups);
        log.trace("getUserGroups end");
        return groups;
    }

    @DeleteMapping(value = "/{userGroupId}/members/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user-group.api.deleteMember}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(null, null, #userGroupId), 'Selc:ManageProductGroups')")
    public void deleteMemberFromUserGroup(@ApiParam("${swagger.dashboard.user-group.model.id}")
                                          @PathVariable("userGroupId") String userGroupId,
                                          @ApiParam("${swagger.dashboard.user.model.id}")
                                          @PathVariable("userId") UUID memberId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup userGroupId = {}, memberId = {}", userGroupId, memberId);
        groupService.deleteMemberFromUserGroup(userGroupId, memberId);
        log.trace("deleteMemberFromUserGroup end");
    }

}
