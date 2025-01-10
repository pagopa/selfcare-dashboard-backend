package it.pagopa.selfcare.dashboard.model.user;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.model.institution.RelationshipState;
import it.pagopa.selfcare.onboarding.common.Env;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * OnboardedProductWithActions
 */
@Data
public class OnboardedProductWithActions {

  private String productId;

  private String tokenId;

  private RelationshipState status;

  private String productRole;

  private PartyRole role;

  private Env env;

  private OffsetDateTime createdAt;

  private OffsetDateTime updatedAt;

  private String delegationId;

  private List<String> userProductActions;

}

