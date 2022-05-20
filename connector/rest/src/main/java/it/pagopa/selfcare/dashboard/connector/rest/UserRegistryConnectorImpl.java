package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.SaveUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
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
    public User search(String fiscalCode, EnumSet<User.Fields> fieldList) {
        log.trace("getUserByExternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId externalId = {}", fiscalCode);
        Assert.hasText(fiscalCode, "A TaxCode is required");
        Assert.notEmpty(fieldList, "At least one user fields is required");
        User user = restClient.search(new EmbeddedExternalId(fiscalCode), fieldList);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId result = {}", user);
        log.trace("getUserByExternalId end");

        return user;
    }

    @Override
    public User getUserByInternalId(String userId, EnumSet<User.Fields> fieldList) {
        log.trace("getUserByInternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId userId = {}", userId);
        Assert.hasText(userId, "A userId is required");
        Assert.notEmpty(fieldList, "At least one user fields is required");
        User result = restClient.getUserByInternalId(UUID.fromString(userId), fieldList);
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

    @Override
    public void deleteById(String userId) {
        log.trace("deleteById start");
        log.debug("deleteById id = {}", userId);
        Assert.hasText(userId, "A UUID is required");
        restClient.deleteById(UUID.fromString(userId));
        log.trace("deleteById end");
    }


}
