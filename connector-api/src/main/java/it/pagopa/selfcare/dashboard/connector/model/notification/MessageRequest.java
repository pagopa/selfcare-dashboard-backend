package it.pagopa.selfcare.dashboard.connector.model.notification;

import lombok.Data;

@Data
public class MessageRequest {

    private String content;
    private String subject;
    private String receiverEmail;

}
