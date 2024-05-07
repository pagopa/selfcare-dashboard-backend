package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.delegation.GetDelegationParameters;

import java.util.List;

public interface DelegationService {

    DelegationId createDelegation(DelegationRequest delegation);

    List<Delegation> getDelegations(GetDelegationParameters delegationParameters);

}
