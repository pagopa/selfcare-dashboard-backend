package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserV2ServiceImpl implements UserV2Service {

    private final MsCoreConnector msCoreConnector;
    private final UserApiConnector userApiConnector;

    @Override
    public void updateUser(UUID id, String institutionId, MutableUserFieldsDto userDto) {
        log.trace("updateUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "updateUser id = {}, institutionId = {}, userDto = {}", id, institutionId, userDto);
        Assert.notNull(id, "UUID is required");
        Assert.hasText(institutionId, "An institutionId is required");
        Assert.notNull(userDto, "A userDto is required");
        Institution institution = msCoreConnector.getInstitution(institutionId);
        if (institution == null) {
            throw new ResourceNotFoundException("There are no institution for given institutionId");
        }
        userApiConnector.updateUser(id.toString(), institutionId, userDto);
        log.trace("updateUser end");
    }

    @Override
    public Collection<InstitutionInfo> getInstitutions(String userId) {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = userApiConnector.getUserProducts(userId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

}
