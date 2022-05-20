package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;

import java.util.Set;

public interface NotificationService {

    void sendCreatedUserNotification(String institutionId, String productTitle, String email, Set<CreateUserDto.Role> productRoles);

    void sendActivatedUserNotification(String relationshipId);

    void sendDeletedUserNotification(String relationshipId);

    void sendSuspendedUserNotification(String relationshipId);


}
