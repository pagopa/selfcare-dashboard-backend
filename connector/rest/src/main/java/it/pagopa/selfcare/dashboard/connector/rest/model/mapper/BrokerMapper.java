package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerResource;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BrokerMapper {

    @Mapping(source = "brokerCode", target = "code")
    BrokerInfo fromBrokerResource(BrokerResource brokerResource);

    @Mapping(source = "brokerPspCode", target = "code")
    BrokerInfo fromBrokerPSPResource(BrokerPspResource brokerPspResource);

}
