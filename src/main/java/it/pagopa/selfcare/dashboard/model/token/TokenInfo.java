package it.pagopa.selfcare.dashboard.model.token;

import it.pagopa.selfcare.dashboard.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.model.InstitutionUpdate;
import it.pagopa.selfcare.dashboard.model.relationship.RelationshipBinding;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TokenInfo {

    private UUID id;
    private String checksum;
    private List<RelationshipBinding> legals;
    private RelationshipState status;
    private String institutionId;
    private String productId;
    private InstitutionUpdate institutionUpdate;

}
