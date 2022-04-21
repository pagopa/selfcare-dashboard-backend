package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserResource;
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
        //TODO add check for insititution
        Institution institution = partyConnector.getInstitution(institutionId);
        userConnector.updateUser(id, institutionId, UserMapper.map(userDto));
        log.trace("updateUser end");
    }

}
