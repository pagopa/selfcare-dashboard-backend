package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "${rest-client.user-registry.serviceCode}", url = "${rest-client.user-registry.base-url}")
public interface UserRegistryRestClient {

    @PostMapping(value = "${rest-client.user-registry.getUserByExternalId.path}")
    @ResponseBody
    UserResponse getUserByExternalId(@RequestBody EmbeddedExternalId externalId);

    @PatchMapping(value = "${rest-client.user-registry.patchUser.path}")
    @ResponseBody
    void patchUser(@PathVariable("id") UUID id,
                   @RequestBody UserRequestDto userRequestDto);

    @GetMapping(value = "${rest-client.user-registry.getUserByInternalId.path}")
    @ResponseBody
    UserResponse getUserByInternalId(@PathVariable("id") String id);
}
