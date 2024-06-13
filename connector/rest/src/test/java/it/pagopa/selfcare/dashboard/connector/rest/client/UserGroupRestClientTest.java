package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupStatus;
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
import org.springframework.data.domain.Page;
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

import static org.junit.jupiter.api.Assertions.*;

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
        FULLY_NULL
    }

    private static final Map<TestCase, String> testCase2igroupdMap = new EnumMap<>(TestCase.class) {{
        put(TestCase.FULLY_VALUED, "groupId1");
        put(TestCase.FULLY_NULL, "groupId2");
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
        assertNotNull(response);
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getCreatedBy());
        assertNotNull(response.getDescription());
        assertNotNull(response.getId());
        assertNotNull(response.getMembers());
        assertNotNull(response.getName());
        assertNotNull(response.getInstitutionId());
        assertNotNull(response.getModifiedAt());
        assertNotNull(response.getModifiedBy());
        assertNotNull(response.getStatus());

    }

    @Test
    void getUserGroupInfo_fullyNull() {
        //given
        String groupId = testCase2igroupdMap.get(TestCase.FULLY_NULL);
        //when
        UserGroupResponse response = restClient.getUserGroupById(groupId);
        //then
        assertNotNull(response);
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
        // when
        Page<UserGroupResponse> response = restClient.getUserGroups(institutionId, productId, userId, List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED), pageable);
        //then
        assertNotNull(response);
        assertEquals(0, response.getNumber());
        assertEquals(20, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertNotNull(response.getContent());
        assertNotNull(response.getContent().get(0).getCreatedAt());
        assertNotNull(response.getContent().get(0).getCreatedBy());
        assertNotNull(response.getContent().get(0).getDescription());
        assertNotNull(response.getContent().get(0).getId());
        assertNotNull(response.getContent().get(0).getMembers());
        assertNotNull(response.getContent().get(0).getName());
        assertNotNull(response.getContent().get(0).getInstitutionId());
        assertNotNull(response.getContent().get(0).getModifiedAt());
        assertNotNull(response.getContent().get(0).getModifiedBy());
        assertNotNull(response.getContent().get(0).getStatus());
    }

    @Test
    void getUserGroups_fullyValuedPageable() {
        //given
        String institutionId = null;
        String productId = null;
        UUID userId = null;
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));
        // when
        Page<UserGroupResponse> response = restClient.getUserGroups(institutionId, productId, userId, List.of(UserGroupStatus.ACTIVE, UserGroupStatus.SUSPENDED), pageable);
        //then
        assertNotNull(response);
        assertEquals(0, response.getNumber());
        assertEquals(20, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertNotNull(response.getContent());
        assertNotNull(response.getContent().get(0).getCreatedAt());
        assertNotNull(response.getContent().get(0).getCreatedBy());
        assertNotNull(response.getContent().get(0).getDescription());
        assertNotNull(response.getContent().get(0).getId());
        assertNotNull(response.getContent().get(0).getMembers());
        assertNotNull(response.getContent().get(0).getName());
        assertNotNull(response.getContent().get(0).getInstitutionId());
        assertNotNull(response.getContent().get(0).getModifiedAt());
        assertNotNull(response.getContent().get(0).getModifiedBy());
        assertNotNull(response.getContent().get(0).getStatus());
    }

    @Test
    void deleteMemberFromUserGroups() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> restClient.deleteMembers(memberId, institutionId, productId);
        //then
        assertDoesNotThrow(executable);
    }

}