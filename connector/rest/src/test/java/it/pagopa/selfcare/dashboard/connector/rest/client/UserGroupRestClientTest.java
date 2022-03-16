package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.UserGroupRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupRequestDto;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestPropertySource(
        locations = "classpath:config/user-group-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        }
)
@ContextConfiguration(
        initializers = UserGroupRestClientTest.RandomPortInitializer.class,
        classes = {UserGroupRestClientTestConfig.class, HttpClientConfiguration.class})
public class UserGroupRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs/user-group");
        wireMockRule = new WireMockClassRule(config);
    }

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_USER_GROUP_URL=http://%s:%d",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
            );
        }
    }

    @Autowired
    private UserGroupRestClient restClient;

    @Test
    public void createGroup() {
        //given
        UserGroupRequestDto request = TestUtils.mockInstance(new UserGroupRequestDto());
        request.setMembers(List.of(TestUtils.mockInstance(UUID.randomUUID().toString())));
        //when
        Executable executable = () -> restClient.createUserGroup(request);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    public void deleteUserGroup() {
        //given
        String id = "id";
        //when
        Executable executable = () -> restClient.deleteUserGroupById(id);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    public void activateUserGroup() {
        //given
        String id = "id";
        //when
        Executable executable = () -> restClient.activateUserGroupById(id);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    public void suspendUserGroup() {
        //given
        String id = "id";
        //when
        Executable executable = () -> restClient.suspendUserGroupById(id);
        //then
        assertDoesNotThrow(executable);
    }

}