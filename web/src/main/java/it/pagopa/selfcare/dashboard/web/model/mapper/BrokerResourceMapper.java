package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.web.model.product.BrokerResource;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BrokerResourceMapper {

    BrokerResource toResource(BrokerInfo brokerInfo);
    List<BrokerResource> toResourceList(List<BrokerInfo> brokerInfos);
    
}
