package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;

import java.util.List;

public interface DelegationService {

    DelegationId createDelegation(Delegation delegation);

    List<Delegation> getDelegations(String from, String productId);
}
