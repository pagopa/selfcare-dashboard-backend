package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.UserRegistryRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestPropertySource(
        locations = "classpath:config/user-registry-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        }
)
@ContextConfiguration(
        initializers = UserRegistryRestClientTest.RandomPortInitializer.class,
        classes = {UserRegistryRestClientTestConfig.class, HttpClientConfiguration.class})
class UserRegistryRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/user-registry"))
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_USER_REGISTRY_URL=%s/pdnd-interop-uservice-user-registry/0.0.1",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }

    @Autowired
    private UserRegistryRestClient restClient;

    @Test
    void userUpdate() {
        //given
        UserRequestDto userRequestDto = new UserRequestDto();
        UUID id = UUID.randomUUID();
        Map<String, Object> cFields = new HashMap<>();
        cFields.put("name", "name");
        cFields.put("surname", "surname");
        cFields.put("institutionContacts.institutionId.email", "email");
        userRequestDto.setCFields(cFields);
        //when
        Executable executable = () -> restClient.patchUser(id, userRequestDto);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void getUserByInternalId() {
        //given
        String userId = "userId";
        //when
        UserResponse response = restClient.getUserByInternalId(userId);
        //then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCertification());
        Assert.assertNotNull(response.getId());
        Assert.assertNotNull(response.getName());
        Assert.assertNotNull(response.getExtras());
        Assert.assertNotNull(response.getSurname());
        Assert.assertNotNull(response.getExternalId());
    }


}