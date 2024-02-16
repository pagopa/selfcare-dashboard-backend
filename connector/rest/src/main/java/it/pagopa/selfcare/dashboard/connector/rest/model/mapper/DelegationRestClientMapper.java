package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DelegationRestClientMapper {
    Delegation toDelegations (it.pagopa.selfcare.core.generated.openapi.v1.dto.DelegationResponse delegationResponse);

    //TODO: TEST
    @Mapping(target = "type", expression = "java(toDelegationType(delegation.getType()))")
    DelegationRequest toDelegationRequest(it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest delegation);

    @Named("toInstitutionType")
    default DelegationRequest.TypeEnum toDelegationType(DelegationType type) {
        if(type != null) {
            return DelegationRequest.TypeEnum.valueOf(type.name());
        }
        return null;
    }
}
