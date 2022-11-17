package it.pagopa.selfcare.dashboard.connector.rest.model.relationship;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import lombok.Data;

import java.util.UUID;

@Data
public class RelationshipBinding {

    private UUID partyId;
    private UUID relationshipId;
    private PartyRole role;

}
