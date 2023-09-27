package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserServiceImpl implements UserService {

    private final UserRegistryConnector userConnector;
    private final MsCoreConnector msCoreConnector;

    @Autowired
    public UserServiceImpl(UserRegistryConnector userConnector, MsCoreConnector msCoreConnector) {
        this.userConnector = userConnector;
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public User search(String fiscalCode) {
        log.trace("getUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUser externalId = {}", fiscalCode);
        Assert.hasText(fiscalCode, "A TaxCode is required");
        User result = userConnector.search(fiscalCode,
                EnumSet.of(name, familyName, email, workContacts));
        Optional.ofNullable(result)
                .ifPresent(user -> user.setFiscalCode(fiscalCode));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUser result = {}", result);
        log.trace("getUser end");
        return result;
    }

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
        msCoreConnector.updateUser(id.toString(), institutionId);
        userConnector.updateUser(id, userDto);
        log.trace("updateUser end");
    }

    @Override
    public User getUserByInternalId(UUID id) {
        log.trace("getUserByInternalId start");
        log.debug("getUserByInternalId id = {}", id);
        Assert.notNull(id, "UUID is required");
        User result = userConnector.getUserByInternalId(id.toString(),
                EnumSet.of(name, familyName, email, fiscalCode, workContacts));
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId result = {}", result);
        log.trace("getUserByInternalId end");
        return result;
    }

    @Override
    public UserId saveUser(String institutionId, SaveUserDto userDto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser institutionId = {}, userDto = {}", institutionId, userDto);
        Assert.hasText(institutionId, "An institutionId is required");
        Assert.notNull(userDto, "A userDto is required");
        Institution institution = msCoreConnector.getInstitution(institutionId);
        if (institution == null) {
            throw new ResourceNotFoundException("There are no institution for given institutionId");
        }
        UserId result = userConnector.saveUser(userDto);
        log.debug("saveUser result = {}", result);
        log.trace("saveUser end");
        return result;
    }

    @Override
    public void deleteById(String userId) {
        log.trace("deleteById start");
        log.debug("deleteById userId = {}", userId);
        Assert.hasText(userId, "A UUID is required");
        userConnector.deleteById(userId);
        log.trace("deleteById end");
    }

    @Override
    public UserInfo findByRelationshipId(String relationshipId, EnumSet<User.Fields> fieldList) {
        log.trace("findByRelationshipId start");
        log.debug("findByRelationshipId = {}", relationshipId);
        final UserInfo userInfo = msCoreConnector.getUser(relationshipId);
        userInfo.setUser(userConnector.getUserByInternalId(userInfo.getId(), fieldList));
        log.debug("findByRelationshipId result = {}", userInfo);
        log.trace("findByRelationshipId end");
        return userInfo;
    }

    @Override
    public Collection<UserInfo> findByInstitutionId(String institutionId, UserInfo.UserInfoFilter userInfoFilter, EnumSet<User.Fields> fieldList) {
        log.trace("findByInstitutionId start");
        log.debug("findByInstitutionId institutionId = {}, role = {}, productId = {}, productRoles = {}, userId = {}", institutionId, userInfoFilter.getRole(), userInfoFilter.getProductId(), userInfoFilter.getProductRoles(), userInfoFilter.getUserId());
        Assert.hasText(institutionId, "An Institution id is required");
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter).stream()
                .peek(userInfo -> {
                    userInfo.setUser(userConnector.getUserByInternalId(userInfo.getId(), fieldList));
                }).collect(Collectors.toList());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "findByInstitutionId result = {}", userInfos);
        log.trace("findByInstitutionId end");
        return userInfos;
    }

}
