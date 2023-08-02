package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationIdResource;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationResource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DelegationMapper {

    DelegationRequest toDelegation(DelegationRequestDto request);
    DelegationIdResource toIdResource(DelegationId delegationId);
    DelegationResource toDelegationResource(Delegation delegation);
}
