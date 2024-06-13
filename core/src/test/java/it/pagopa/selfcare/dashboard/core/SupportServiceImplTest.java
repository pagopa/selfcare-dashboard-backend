package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportResponse;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;

import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupportServiceImplTest extends BaseServiceTest {

    @InjectMocks
    SupportServiceImpl supportServiceImpl;
    @Mock
    private UserRegistryConnector userRegistryConnectorMock;
    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, fiscalCode);

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void sendRequest() throws IOException {

        ReflectionTestUtils.setField(supportServiceImpl, "supportApiKey", "testSupportApiKey");
        ReflectionTestUtils.setField(supportServiceImpl, "returnTo", "testReturnTo");
        ReflectionTestUtils.setField(supportServiceImpl, "zendeskOrganization", "testZendeskOrganization");
        ReflectionTestUtils.setField(supportServiceImpl, "actionUrl", "testActionUrl");

        SupportResponse expectation = new SupportResponse();
        expectation.setRedirectUrl("testReturnTo?product=productId&institution=institutionId");
        expectation.setActionUrl("testActionUrl");


        ClassPathResource resource = new ClassPathResource("expectations/SupportRequest.json");
        byte[] supportRequestStream = Files.readAllBytes(resource.getFile().toPath());
        SupportRequest supportRequest = objectMapper.readValue(supportRequestStream, new TypeReference<>() {
        });

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, new TypeReference<>() {
        });

        when(userRegistryConnectorMock.getUserByInternalId(supportRequest.getUserId(), USER_FIELD_LIST)).thenReturn(user);

        SupportResponse response = supportServiceImpl.sendRequest(supportRequest);

        Assertions.assertEquals(expectation.getRedirectUrl(), response.getRedirectUrl());
        Assertions.assertEquals(expectation.getActionUrl(), response.getActionUrl());
        Assertions.assertNotNull(response.getJwt());
        Mockito.verify(userRegistryConnectorMock, Mockito.times(1)).getUserByInternalId(supportRequest.getUserId(), USER_FIELD_LIST);
    }

    @Test
    void sendRequest_userIdEmpty() throws IOException {

        ClassPathResource resource = new ClassPathResource("expectations/SupportRequest.json");
        byte[] supportRequestStream = Files.readAllBytes(resource.getFile().toPath());
        SupportRequest supportRequest = objectMapper.readValue(supportRequestStream, new TypeReference<>() {
        });

        when(userRegistryConnectorMock.getUserByInternalId(supportRequest.getUserId(), USER_FIELD_LIST)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> supportServiceImpl.sendRequest(supportRequest));
    }

    @Test
    void sendRequest_SupportApiKeyEmpty() throws IOException {

        ReflectionTestUtils.setField(supportServiceImpl, "supportApiKey", null);
        ReflectionTestUtils.setField(supportServiceImpl, "returnTo", "testReturnTo");
        ReflectionTestUtils.setField(supportServiceImpl, "zendeskOrganization", "testZendeskOrganization");
        ReflectionTestUtils.setField(supportServiceImpl, "actionUrl", "testActionUrl");

        ClassPathResource resource = new ClassPathResource("expectations/SupportRequest.json");
        byte[] supportRequestStream = Files.readAllBytes(resource.getFile().toPath());
        SupportRequest supportRequest = objectMapper.readValue(supportRequestStream, new TypeReference<>() {
        });

        ClassPathResource userResource = new ClassPathResource("expectations/User.json");
        byte[] userStream = Files.readAllBytes(userResource.getFile().toPath());
        User user = objectMapper.readValue(userStream, new TypeReference<>() {
        });

        when(userRegistryConnectorMock.getUserByInternalId(supportRequest.getUserId(), USER_FIELD_LIST)).thenReturn(user);

        assertThrows(SupportException.class, () -> supportServiceImpl.sendRequest(supportRequest));
    }
}
