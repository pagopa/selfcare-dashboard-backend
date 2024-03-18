package it.pagopa.selfcare.dashboard.connector.model.user;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.utils.Env;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class OnboardedProduct {

    private String productId;
    private String tokenId;
    private RelationshipState status;
    private String productRole;
    private PartyRole role;
    private Env env;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}