package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${rest-client.user-groups.serviceCode}", url = "${rest-client.user-groups.base-url}")
public interface UserGroupRestClient {

    @PostMapping(value = "${rest-client.user-groups.createUserGroup.path}")
    @ResponseBody
    UserGroupResponse createUserGroup(@RequestBody UserGroupRequestDto userGroupRequestDto);

    @DeleteMapping(value = "${rest-client.user-group.deleteUserGroup.path}")
    @ResponseBody
    void deleteUserGroupById(@PathVariable("id") String id);
}
