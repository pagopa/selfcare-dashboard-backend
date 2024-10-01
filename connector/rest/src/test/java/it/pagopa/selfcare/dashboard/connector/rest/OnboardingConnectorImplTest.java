package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.rest.client.OnboardingRestClient;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGetResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class OnboardingConnectorImplTest  extends BaseConnectorTest {

    @Mock
    private OnboardingRestClient onboardingRestClient;

    @InjectMocks
    private OnboardingConnectorImpl onboardingConnector;

    @BeforeEach
    public void setup() {
        super.setUp();
    }

    @Test
    void getOnboardingWithFilterOk() throws IOException {
        String taxCode = "taxCode";
        String productId = "productId";
        String status = "status";

        ClassPathResource resource = new ClassPathResource("stubs/OnboardingGetResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());

        OnboardingGetResponse onboardingGetResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        doReturn(ResponseEntity.of(Optional.of(onboardingGetResponse)))
                .when(onboardingRestClient)
                ._getOnboardingWithFilter(null, null, null, null, productId, 1, status, null, taxCode, null);

        Boolean onboardingGetInfo = onboardingConnector.getOnboardingWithFilter(taxCode, null, productId, status);
        Assertions.assertTrue(onboardingGetInfo);
    }

    @Test
    void getOnboardingWithFilterEmpty() throws IOException {
        String taxCode = "taxCode";
        String subunitCode = "subunitCode";
        String productId = "productId";
        String status = "status";

        ClassPathResource resource = new ClassPathResource("stubs/OnboardingGetResponseEmpty.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());

        OnboardingGetResponse onboardingGetResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        doReturn(ResponseEntity.of(Optional.of(onboardingGetResponse))).when(onboardingRestClient)._getOnboardingWithFilter(null, null, null, null, productId, 1, status, subunitCode,  taxCode, null);

        Boolean onboardingGetInfo = onboardingConnector.getOnboardingWithFilter(taxCode, subunitCode, productId, status);

        assertFalse(onboardingGetInfo);
    }

}
