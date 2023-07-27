package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationIdResource;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DelegationMapper {

    Delegation toDelegation(DelegationRequestDto request);
    DelegationIdResource toIdResource(DelegationId delegationId);
    @Mapping(target = "institutionName", source = "institutionFromName")
    @Mapping(target = "partnerName", source = "institutionToName")
    DelegationResource toDelegationResource(Delegation delegation);
}
