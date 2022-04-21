package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.SaveUserDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserId;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.UUID;

@FeignClient(name = "${rest-client.user-registry.serviceCode}", url = "${rest-client.user-registry.base-url}")
public interface UserRegistryRestClient {

    @PostMapping(value = "${rest-client.user-registry.getUserByExternalId.path}")
    @ResponseBody
    UserResource getUserByExternalId(@RequestParam(value = "fl") EnumSet<UserResource.Fields> fields,
                                     @RequestBody EmbeddedExternalId externalId);

    @PatchMapping(value = "${rest-client.user-registry.patchUser.path}")
    @ResponseBody
    void patchUser(@PathVariable("id") UUID id,
                   @RequestBody MutableUserFieldsDto request);

    @GetMapping(value = "${rest-client.user-registry.getUserByInternalId.path}")
    @ResponseBody
    UserResource getUserByInternalId(@PathVariable("id") UUID id,
                                     @RequestParam(value = "fl") EnumSet<UserResource.Fields> fieldList);

    @PatchMapping(value = "${rest-client.user-registry.saveUser.path}")
    @ResponseBody
    UserId saveUser(@RequestBody SaveUserDto request);

    @DeleteMapping(value = "${rest-client.user-registry.deleteUserById.path}")
    @ResponseBody
    void deleteById(@PathVariable("id") UUID id);

}
