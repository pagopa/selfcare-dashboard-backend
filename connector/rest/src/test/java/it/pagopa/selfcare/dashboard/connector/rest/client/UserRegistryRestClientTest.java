package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.UserRegistryRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserRequestDto;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestPropertySource(
        locations = "classpath:config/user-registry-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest"
        }
)
@ContextConfiguration(
        initializers = UserRegistryRestClientTest.RandomPortInitializer.class,
        classes = {UserRegistryRestClientTestConfig.class})
class UserRegistryRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    @Autowired
    private UserRegistryRestClient restClient;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs/user-registry");
        wireMockRule = new WireMockClassRule(config);
    }

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("USERVICE_USER_REGISTRY_URL:http://%s:%d/pdnd-interop-uservice-user-registry-management/0.0.1",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
            );
        }

    }

    @Test
    public void userUpdate() {
        //given
        UserRequestDto userRequestDto = new UserRequestDto();
        UUID id = UUID.randomUUID();
        userRequestDto.setCFields(TestUtils.mockInstance(new HashMap<>()));
        //when
        Executable executable = () -> restClient.patchUser(id, userRequestDto);
        //then
        assertDoesNotThrow(executable);
    }

}