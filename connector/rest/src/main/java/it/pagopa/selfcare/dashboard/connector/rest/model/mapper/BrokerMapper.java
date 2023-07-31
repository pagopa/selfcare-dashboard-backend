package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerResource;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BrokerMapper {

    @Mapping(source = "brokerCode", target = "code")
    BrokerInfo fromBrokerResource(BrokerResource brokerResource);

    @Mapping(source = "id", target = "code")
    BrokerInfo fromInstitution(Institution institution);
    List<BrokerInfo> fromInstitutions(List<Institution> institutions);

    @Mapping(source = "brokerPspCode", target = "code")
    BrokerInfo fromBrokerPSPResource(BrokerPspResource brokerPspResource);

}