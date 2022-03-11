package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.connector.rest.config.NotificationManagerRestClientConfigTest;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

@TestPropertySource(
        locations = "classpath:config/notification-manager-rest-client.properties",
        properties = {"logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest"}
)
@ContextConfiguration(
        initializers = NotificationManagerRestClientTest.RandomPortInitializer.class,
        classes = {NotificationManagerRestClientConfigTest.class}
)
public class NotificationManagerRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs/notification-manager");
        wireMockRule = new WireMockClassRule(config);
    }

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_NOTIFICATION_MANAGER_URL=http://%s:%d",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
            );
        }
    }

    @Autowired
    private NotificationManagerRestClient restClient;

    @Test
    public void sendNotificationToUser() {
        //given
        MessageRequest message = TestUtils.mockInstance(new MessageRequest());
        //when
        Executable executable = () -> restClient.sendNotificationToUser(message);
        //then
        Assertions.assertDoesNotThrow(executable);

    }

}