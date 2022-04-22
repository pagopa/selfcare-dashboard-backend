package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.core.model.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

@Slf4j
@Service
public class UserRegistryServiceImpl implements UserRegistryService {

    private final UserRegistryConnector userConnector;
    private final PartyConnector partyConnector;

    @Autowired
    public UserRegistryServiceImpl(UserRegistryConnector userConnector, PartyConnector partyConnector) {
        this.userConnector = userConnector;
        this.partyConnector = partyConnector;
    }

    @Override
    public UserResource search(String externalId) {
        log.trace("getUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUser externalId = {}", externalId);
        Assert.hasText(externalId, "A TaxCode is required");
        UserResource result = userConnector.search(externalId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUser result = {}", result);
        log.trace("getUser end");
        return result;
    }

    @Override
    public void updateUser(UUID id, String institutionId, UserDto userDto) {
        log.trace("updateUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "updateUser id = {}, institutionId = {}, userDto = {}", id, institutionId, userDto);
        Assert.notNull(id, "UUID is required");
        Assert.hasText(institutionId, "An institutionId is required");
        Assert.notNull(userDto, "A userDto is required");
        Institution institution = partyConnector.getInstitution(institutionId);
        if (institution == null) {
            throw new ResourceNotFoundException("There are no institution for given institutionId");
        }
        userConnector.updateUser(id, UserMapper.map(userDto));
        log.trace("updateUser end");
    }

    @Override
    public UserResource getUserByInternalId(UUID id) {
        log.trace("getUserByInternalId start");
        log.debug("getUserByInternalId id = {}", id);
        Assert.notNull(id, "UUID is required");
        UserResource result = userConnector.getUserByInternalId(id.toString());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId result = {}", result);
        log.trace("getUserByInternalId end");
        return result;
    }

    @Override
    public UserId saveUser(String institutionId, SaveUser userDto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser institutionId = {}, userDto = {}", institutionId, userDto);
        Assert.hasText(institutionId, "An institutionId is required");
        Assert.notNull(userDto, "A userDto is required");
        Institution institution = partyConnector.getInstitution(institutionId);
        if (institution == null) {
            throw new ResourceNotFoundException("There are no institution for given institutionId");
        }
        SaveUserDto dto = UserMapper.map(userDto);
        UserId result = userConnector.saveUser(dto);
        log.debug("saveUser result = {}", result);
        log.trace("saveUser end");
        return result;
    }

}
