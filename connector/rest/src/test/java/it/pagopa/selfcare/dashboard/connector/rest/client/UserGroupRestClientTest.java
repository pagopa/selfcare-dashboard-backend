package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.UserGroupRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import lombok.SneakyThrows;
import org.junit.Assert;
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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

    private enum TestCase {
        FULLY_VALUED,
        FULLY_NULL,
        EMPTY_RESULT
    }

    private static final Map<UserGroupRestClientTest.TestCase, String> testCase2igroupdMap = new EnumMap<>(UserGroupRestClientTest.TestCase.class) {{
        put(UserGroupRestClientTest.TestCase.FULLY_VALUED, "groupId1");
        put(UserGroupRestClientTest.TestCase.FULLY_NULL, "groupId2");
    }};
    @Autowired
    private UserGroupRestClient restClient;

    @Test
    public void createGroup() {
        //given
        CreateUserGroupRequestDto request = TestUtils.mockInstance(new CreateUserGroupRequestDto());
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

    @Test
    public void updateUserGroup() {
        //given
        String id = "id";
        UpdateUserGroupRequestDto request = TestUtils.mockInstance(new UpdateUserGroupRequestDto());
        request.setMembers(List.of(TestUtils.mockInstance(UUID.randomUUID().toString())));
        //when
        Executable executable = () -> restClient.updateUserGroupById(id, request);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    public void getUserGroupInfo_fullyValued() {
        //given
        String groupId = testCase2igroupdMap.get(TestCase.FULLY_VALUED);
        //when
        UserGroupResponse response = restClient.getUserGroupById(groupId);
        //then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCreatedAt());
        Assert.assertNotNull(response.getCreatedBy());
        Assert.assertNotNull(response.getDescription());
        Assert.assertNotNull(response.getId());
        Assert.assertNotNull(response.getMembers());
        Assert.assertNotNull(response.getName());
        Assert.assertNotNull(response.getInstitutionId());
        Assert.assertNotNull(response.getModifiedAt());
        Assert.assertNotNull(response.getModifiedBy());
        Assert.assertNotNull(response.getStatus());

    }

    @Test
    public void getUserGroupInfo_fullyNull() {
        //given
        String groupId = testCase2igroupdMap.get(TestCase.FULLY_NULL);
        //when
        UserGroupResponse response = restClient.getUserGroupById(groupId);
        //then
        Assert.assertNotNull(response);
        Assert.assertNull(response.getCreatedAt());
        Assert.assertNull(response.getCreatedBy());
        Assert.assertNull(response.getDescription());
        Assert.assertNull(response.getId());
        Assert.assertNull(response.getMembers());
        Assert.assertNull(response.getName());
        Assert.assertNull(response.getInstitutionId());
        Assert.assertNull(response.getModifiedAt());
        Assert.assertNull(response.getModifiedBy());
        Assert.assertNull(response.getStatus());
    }

    @Test
    public void addMemberToUserGroup() {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> restClient.addMemberToUserGroup(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    public void deleteMemberFromUserGroup() {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> restClient.deleteMemberFromUserGroup(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
    }

}