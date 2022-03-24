package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
class RelationshipServiceImpl implements RelationshipService {

    private final PartyConnector partyConnector;
    private final UserGroupService userGroupService;
    private final NotificationService notificationService;
    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";

    @Autowired
    RelationshipServiceImpl(PartyConnector partyConnector, UserGroupService userGroupService, NotificationService notificationService) {
        this.partyConnector = partyConnector;
        this.userGroupService = userGroupService;
        this.notificationService = notificationService;
    }


    @Override
    public void suspend(String relationshipId) {
        log.trace("suspend start");
        log.debug("suspend relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        partyConnector.suspend(relationshipId);
        notificationService.sendSuspendedUserNotification(relationshipId);
        log.trace("suspend end");

    }


    @Override
    public void activate(String relationshipId) {
        log.trace("activate start");
        log.debug("activate relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        partyConnector.activate(relationshipId);
        notificationService.sendActivatedUserNotification(relationshipId);
        log.trace("activate end");

    }

    @Override
    public void delete(String relationshipId) {
        log.trace("delete start");
        log.debug("relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);

        UserInfo user = partyConnector.getUser(relationshipId);

        partyConnector.delete(relationshipId);
        notificationService.sendDeletedUserNotification(relationshipId);

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        String productId = user.getProducts().keySet().iterator().next();
        userInfoFilter.setProductId(Optional.ofNullable(productId));
        userInfoFilter.setUserId(Optional.ofNullable(user.getId()));
        Collection<UserInfo> users = partyConnector.getUsers(user.getInstitutionId(), userInfoFilter);
        if (users.isEmpty()) {
            userGroupService.deleteMembers(user.getId(), user.getInstitutionId(), productId);
        }
        log.trace("delete end");
    }


}
