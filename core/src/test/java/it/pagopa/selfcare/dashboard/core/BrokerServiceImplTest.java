package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.PagoPABackOfficeConnector;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrokerServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private BrokerServiceImpl brokerService;
    @Mock
    PagoPABackOfficeConnector backOfficeConnectorMock;
    @Mock
    MsCoreConnector msCoreConnector;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getBrokersPSP() throws IOException {

        String institutionType = "PSP";

        ClassPathResource pathResource = new ClassPathResource("expectations/BrokerInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        List<BrokerInfo> brokerInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(backOfficeConnectorMock.getBrokersPSP(0, 1000))
                .thenReturn(brokerInfo);

        List<BrokerInfo> result = brokerService.findAllByInstitutionType(institutionType);
        Assertions.assertEquals(brokerInfo, result);
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersPSP(0, 1000);

    }

    @Test
    void getBrokersEC() throws IOException {

        String institutionType = "EC";

        ClassPathResource pathResource = new ClassPathResource("expectations/BrokerInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        List<BrokerInfo> brokerInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(backOfficeConnectorMock.getBrokersEC(0, 1000))
                .thenReturn(brokerInfo);

        List<BrokerInfo> brokers = brokerService.findAllByInstitutionType(institutionType);
        Assertions.assertEquals(brokerInfo, brokers);
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersEC(0, 1000);

    }

    @Test
    void getBrokerEmptyList() {
        List<BrokerInfo> brokers = brokerService.findAllByInstitutionType(null);
        Assertions.assertEquals(0, brokers.size());
        Mockito.verify(backOfficeConnectorMock, Mockito.times(1))
                .getBrokersEC(0, 1000);
    }

    @Test
    void getFindInstitutionsByProductAndType() throws IOException {
        String institutionType = "EC";
        String productId = "productId";

        ClassPathResource pathResource = new ClassPathResource("expectations/BrokerInfo.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        List<BrokerInfo> brokerInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreConnector.findInstitutionsByProductAndType(productId, institutionType))
                .thenReturn(brokerInfo);

        List<BrokerInfo> brokers = brokerService.findInstitutionsByProductAndType(productId, institutionType);
        Assertions.assertEquals(brokerInfo, brokers);
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .findInstitutionsByProductAndType(productId, institutionType);
    }

    @Test
    void getFindInstitutionsByProductAndTypeEmptyList() {

        List<BrokerInfo> brokers = brokerService.findInstitutionsByProductAndType(null, null);
        Assertions.assertEquals(0, brokers.size());
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .findInstitutionsByProductAndType(null, null);
    }
}
