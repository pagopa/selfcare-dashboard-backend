package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${rest-client.user-groups.serviceCode}", url = "${rest-client.user-groups.base-url}")
public interface UserGroupRestClient {

    @PostMapping(value = "${rest-client.user-groups.createUserGroup.path}")
    @ResponseBody
    UserGroupResponse createUserGroup(@RequestBody CreateUserGroupRequestDto createUserGroupRequestDto);

    @DeleteMapping(value = "${rest-client.user-group.deleteUserGroup.path}")
    @ResponseBody
    void deleteUserGroupById(@PathVariable("id") String id);

    @PostMapping(value = "${rest-client.user-group.activateUserGroup.path}")
    @ResponseBody
    void activateUserGroupById(@PathVariable("id") String id);

    @PostMapping(value = "${rest-client.user-group.suspendUserGroup.path}")
    @ResponseBody
    void suspendUserGroupById(@PathVariable("id") String id);

    @PutMapping(value = "${rest-client.user-group.updateUserGroup.path}")
    @ResponseBody
    void updateUserGroupById(@PathVariable("id") String id,
                             @RequestBody UpdateUserGroupRequestDto updateUserGroupRequestDto);

    @GetMapping(value = "${rest-client.user-group.getUSerGroupById.path}")
    @ResponseBody
    UserGroupResponse getUserGroupById(@PathVariable("id") String id);
}
