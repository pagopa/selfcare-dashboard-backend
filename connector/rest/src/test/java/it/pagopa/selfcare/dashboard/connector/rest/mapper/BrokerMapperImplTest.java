package it.pagopa.selfcare.dashboard.connector.rest.mapper;

import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerPspResource;
import it.pagopa.selfcare.backoffice.generated.openapi.v1.dto.BrokerResource;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapperImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BrokerMapperImplTest {

    BrokerMapperImpl mapper = new BrokerMapperImpl();

    @Test
    void fromBrokerResource() {
        BrokerResource resource = new BrokerResource();
        resource.setBrokerCode("id");
        resource.setDescription("description");
        BrokerInfo brokerInfo = mapper.fromBrokerResource(resource);
        assertNotNull(brokerInfo);
        assertEquals(brokerInfo.getCode(), resource.getBrokerCode());
        assertEquals(brokerInfo.getDescription(), resource.getDescription());
    }

    @Test
    void fromBrokerPSPResource() {
        BrokerPspResource resource = new BrokerPspResource();
        resource.setBrokerPspCode("id");
        resource.setDescription("description");
        BrokerInfo brokerInfo = mapper.fromBrokerPSPResource(resource);
        assertNotNull(brokerInfo);
        assertEquals(brokerInfo.getCode(), resource.getBrokerPspCode());
        assertEquals(brokerInfo.getDescription(), resource.getDescription());
    }

    @Test
    void fromInstitution() {
        Institution institution = new Institution();
        institution.setId("id");
        institution.setDescription("description");
        BrokerInfo brokerInfo = mapper.fromInstitution(institution);
        assertNotNull(brokerInfo);
        assertEquals(brokerInfo.getCode(), institution.getId());
        assertEquals(brokerInfo.getDescription(), institution.getDescription());
    }

    @Test
    void fromInstitutions() {
        Institution institution = new Institution();
        institution.setId("id");
        institution.setDescription("description");
        List<BrokerInfo> brokers = mapper.fromInstitutions(List.of(institution));
        assertNotNull(brokers);
        assertNotNull(brokers.get(0));
        assertEquals(1, brokers.size());
        assertEquals(brokers.get(0).getCode(), institution.getId());
        assertEquals(brokers.get(0).getDescription(), institution.getDescription());
    }

}
