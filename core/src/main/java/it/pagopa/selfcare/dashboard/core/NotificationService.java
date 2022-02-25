package it.pagopa.selfcare.dashboard.core;

public interface NotificationService {
    void sendNotificationCreateUserRelationship(String productTitle, String email);

    void sendNotificationDeleteUserRelationship(String relationshipId);

    void sendNotificationSuspendUserRelationship(String relationshipId);
}
