package it.pagopa.selfcare.dashboard.core;

public interface NotificationService {
    void sendNotificationCreateUserRelationship(String productTitle, String email);

    void sendNotificationActivatedRelationship(String relationshipId);

    void sendNotificationDeletedRelationship(String relationshipId);

    void sendNotificationSuspendedRelationship(String relationshipId);


}
