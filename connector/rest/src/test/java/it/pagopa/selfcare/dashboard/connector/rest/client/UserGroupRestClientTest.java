package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.rest.config.UserGroupRestClientTestConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.CreateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UpdateUserGroupRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_group.UserGroupResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
class UserGroupRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/user-group"))
            .build();


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_USER_GROUP_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
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
    void createGroup() {
        //given
        CreateUserGroupRequestDto request = TestUtils.mockInstance(new CreateUserGroupRequestDto());
        request.setMembers(List.of(TestUtils.mockInstance(UUID.randomUUID().toString())));
        //when
        Executable executable = () -> restClient.createUserGroup(request);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void deleteUserGroup() {
        //given
        String id = "id";
        //when
        Executable executable = () -> restClient.deleteUserGroupById(id);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void activateUserGroup() {
        //given
        String id = "id";
        //when
        Executable executable = () -> restClient.activateUserGroupById(id);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void suspendUserGroup() {
        //given
        String id = "id";
        //when
        Executable executable = () -> restClient.suspendUserGroupById(id);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void updateUserGroup() {
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
    void getUserGroupInfo_fullyValued() {
        //given
        String groupId = testCase2igroupdMap.get(TestCase.FULLY_VALUED);
        //when
        UserGroupResponse response = restClient.getUserGroupById(groupId);
        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getCreatedAt());
        Assertions.assertNotNull(response.getCreatedBy());
        Assertions.assertNotNull(response.getDescription());
        Assertions.assertNotNull(response.getId());
        Assertions.assertNotNull(response.getMembers());
        Assertions.assertNotNull(response.getName());
        Assertions.assertNotNull(response.getInstitutionId());
        Assertions.assertNotNull(response.getModifiedAt());
        Assertions.assertNotNull(response.getModifiedBy());
        Assertions.assertNotNull(response.getStatus());

    }

    @Test
    void getUserGroupInfo_fullyNull() {
        //given
        String groupId = testCase2igroupdMap.get(TestCase.FULLY_NULL);
        //when
        UserGroupResponse response = restClient.getUserGroupById(groupId);
        //then
        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getCreatedAt());
        Assertions.assertNull(response.getCreatedBy());
        Assertions.assertNull(response.getDescription());
        Assertions.assertNull(response.getId());
        Assertions.assertNull(response.getMembers());
        Assertions.assertNull(response.getName());
        Assertions.assertNull(response.getInstitutionId());
        Assertions.assertNull(response.getModifiedAt());
        Assertions.assertNull(response.getModifiedBy());
        Assertions.assertNull(response.getStatus());
    }

    @Test
    void addMemberToUserGroup() {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> restClient.addMemberToUserGroup(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void deleteMemberFromUserGroup() {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> restClient.deleteMemberFromUserGroup(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void getUserGroups_fullyValued() {
        //given
        String institutionId = null;
        String productId = null;
        UUID userId = null;
        Pageable pageable = Pageable.unpaged();

        List<UserGroupResponse> response = restClient.getUserGroups(institutionId, productId, userId, pageable);
        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.get(0).getCreatedAt());
        Assertions.assertNotNull(response.get(0).getCreatedBy());
        Assertions.assertNotNull(response.get(0).getDescription());
        Assertions.assertNotNull(response.get(0).getId());
        Assertions.assertNotNull(response.get(0).getMembers());
        Assertions.assertNotNull(response.get(0).getName());
        Assertions.assertNotNull(response.get(0).getInstitutionId());
        Assertions.assertNotNull(response.get(0).getModifiedAt());
        Assertions.assertNotNull(response.get(0).getModifiedBy());
        Assertions.assertNotNull(response.get(0).getStatus());
    }

    @Test
    void getUserGroups_fullyValuedPageable() {
        //given
        String institutionId = null;
        String productId = null;
        UUID userId = null;
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

        List<UserGroupResponse> response = restClient.getUserGroups(institutionId, productId, userId, pageable);
        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.get(0).getCreatedAt());
        Assertions.assertNotNull(response.get(0).getCreatedBy());
        Assertions.assertNotNull(response.get(0).getDescription());
        Assertions.assertNotNull(response.get(0).getId());
        Assertions.assertNotNull(response.get(0).getMembers());
        Assertions.assertNotNull(response.get(0).getName());
        Assertions.assertNotNull(response.get(0).getInstitutionId());
        Assertions.assertNotNull(response.get(0).getModifiedAt());
        Assertions.assertNotNull(response.get(0).getModifiedBy());
        Assertions.assertNotNull(response.get(0).getStatus());
    }

}