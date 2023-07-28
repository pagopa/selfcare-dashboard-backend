package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DelegationRestClientMapper {
    Delegation toDelegations (it.pagopa.selfcare.core.generated.openapi.v1.dto.DelegationResponse delegationResponse);
}
