package it.pagopa.selfcare.dashboard.core;

public interface NotificationService {

    void sendCreatedUserNotification(String institutionId, String productTitle, String email);

    void sendActivatedUserNotification(String relationshipId);

    void sendDeletedUserNotification(String relationshipId);

    void sendSuspendedUserNotification(String relationshipId);


}
