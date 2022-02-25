package it.pagopa.selfcare.dashboard.core;

public interface NotificationService {
    void sendNotificationCreateUserRelationship(String productTitle, String email);


    void sendNotificationRelationshipEvent(String relationshipId, String template);

}
