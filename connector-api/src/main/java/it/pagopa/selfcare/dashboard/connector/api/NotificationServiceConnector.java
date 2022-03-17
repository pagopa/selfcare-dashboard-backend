package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;

public interface NotificationServiceConnector {
    void sendNotificationToUser(MessageRequest messageRequest);
}
