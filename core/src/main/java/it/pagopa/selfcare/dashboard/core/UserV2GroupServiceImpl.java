package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserV2GroupServiceImpl implements UserV2GroupService {

    private final UserGroupConnector groupConnector;
    private final UserApiConnector userApiConnector;

    @Override
    public void deleteMembersByUserId(String userId, String institutionId, String productId) {
        log.trace("deleteMembersByUserId start");
        log.debug("deleteMembersByUserId userId = {}", userId);
        List<UserInstitution> userInstitutionList = userApiConnector.retrieveFilteredUser(userId, institutionId, productId);
        if (CollectionUtils.isEmpty(userInstitutionList)) {
            log.debug("User not found, deleting members for userId = {}", userId);
            groupConnector.deleteMembers(userId, institutionId, productId);
        } else {
            log.debug("User found, not deleting members for userId = {}", userId);
        }
    }
}
