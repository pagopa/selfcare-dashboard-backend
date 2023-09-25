package it.pagopa.selfcare.dashboard.connector.model.support;

import lombok.Data;

@Data
public class SupportRequest {

    private String email;
    private String productId;
    private String name;
    private UserField userFields;

}
