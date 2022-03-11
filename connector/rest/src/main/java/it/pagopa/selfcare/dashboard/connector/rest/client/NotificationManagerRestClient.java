package it.pagopa.selfcare.dashboard.connector.rest.client;

import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Notification Manager Rest Client
 */
@FeignClient(name = "${rest-client.notification-manager.serviceCode}", url = "${rest-client.notification-manager.base-url}")
public interface NotificationManagerRestClient extends NotificationServiceConnector {

    @PostMapping(value = "${rest-client.notification-manager.sendNotificationToUser}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void sendNotificationToUser(@RequestBody MessageRequest messageRequest);

}
