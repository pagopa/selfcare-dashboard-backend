package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.EnumSet;
import java.util.UUID;

@Slf4j
@Service
public class UserRegistryConnectorImpl implements UserRegistryConnector {

    private final UserRegistryRestClient restClient;

    @Autowired
    public UserRegistryConnectorImpl(UserRegistryRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public UserResource search(String externalId) {
        log.trace("getUserByExternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId externalId = {}", externalId);
        Assert.hasText(externalId, "A TaxCode is required");
        UserResource userResource = restClient.search(EnumSet.allOf(UserResource.Fields.class), new EmbeddedExternalId(externalId));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId result = {}", userResource);
        log.trace("getUserByExternalId end");

        return userResource;
    }

    @Override
    public UserResource getUserByInternalId(String userId) {
        log.trace("getUserByInternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId userId = {}", userId);
        Assert.hasText(userId, "A userId is required");
        UserResource result = restClient.getUserByInternalId(UUID.fromString(userId), EnumSet.allOf(UserResource.Fields.class));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId result = {}", result);
        log.trace("getUserByInternalId end");
        return result;
    }

    @Override
    public void updateUser(UUID id, MutableUserFieldsDto userDto) {
        log.trace("update start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "update id = {}, userDto = {}}", id, userDto);
        Assert.notNull(id, "A UUID is required");
        restClient.patchUser(id, userDto);
        log.trace("update end");
    }

    @Override
    public UserId saveUser(SaveUserDto dto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser dto = {}}", dto);
        UserId userId = restClient.saveUser(dto);
        log.debug("saveUser result = {}", userId);
        log.trace("saveUser end");
        return userId;
    }


}
