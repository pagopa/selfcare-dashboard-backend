package it.pagopa.selfcare.dashboard.connector.rest.model.token;

import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.RelationshipBinding;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TokenInfo {

    private UUID id;
    private String checksum;
    private List<RelationshipBinding> legals;

}
