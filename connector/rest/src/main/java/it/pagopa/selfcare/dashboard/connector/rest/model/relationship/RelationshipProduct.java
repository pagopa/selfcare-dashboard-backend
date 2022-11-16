package it.pagopa.selfcare.dashboard.connector.rest.model.relationship;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RelationshipProduct {

    private String id;
    private String role;
    private OffsetDateTime createdAt;

}
