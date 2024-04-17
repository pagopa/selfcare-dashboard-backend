package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;

import java.util.List;

public interface DelegationService {

    DelegationId createDelegation(DelegationRequest delegation);

    List<Delegation> getDelegations(String from, String to, String productId, String search, String taxCode, String mode, String order, Integer page, Integer size);

}
