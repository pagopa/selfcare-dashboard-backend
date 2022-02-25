package it.pagopa.selfcare.dashboard.connector.api;

import lombok.Data;

@Data
public class RelationshipInfoResult {
    private String id;
    private String email;
    private String productRole;
    private String productId;
}
