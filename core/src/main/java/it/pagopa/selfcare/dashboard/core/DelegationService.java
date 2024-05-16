package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.delegation.*;

import java.util.List;

public interface DelegationService {

    DelegationId createDelegation(DelegationRequest delegation);

    List<Delegation> getDelegations(GetDelegationParameters delegationParameters);

    DelegationWithPagination getDelegationsV2(GetDelegationParameters delegationParameters);

}
