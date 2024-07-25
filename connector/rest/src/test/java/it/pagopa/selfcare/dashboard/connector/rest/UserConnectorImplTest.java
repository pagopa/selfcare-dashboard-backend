package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.institution.RootParentResponse;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserPermissionRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapperImpl;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapperImpl;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import wiremock.com.github.jknack.handlebars.internal.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserConnectorImplTest extends BaseConnectorTest {

    @Mock
    private UserApiRestClient userApiRestClient;

    @Mock
    private UserPermissionRestClient userPermissionRestClient;

    @Mock
    private UserInstitutionApiRestClient userInstitutionApiRestClient;

    @InjectMocks
    protected UserConnectorImpl userConnector;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Spy
    private InstitutionMapperImpl institutionMapper = new InstitutionMapperImpl();

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getUserProductsNotFound() {
        String userId = "userId";
        when(userApiRestClient._usersUserIdInstitutionsGet(userId, null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userConnector.getUserInstitutions(userId));
    }

    @Test
    void getUserProductsFound() throws IOException {
        String userId = "userId";

        ClassPathResource resource = new ClassPathResource("stubs/UserInfoResponse.json");
        String expectedResource = StringUtils.deleteWhitespace(new String(Files.readAllBytes(resource.getFile().toPath())));
        UserInfoResponse userInfoResponse = objectMapper.readValue(expectedResource, new TypeReference<>() {});
        when(userApiRestClient._usersUserIdInstitutionsGet(userId, null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name())))
                .thenReturn(ResponseEntity.ok(userInfoResponse));
        List<InstitutionBase> result = userConnector.getUserInstitutions(userId);
        assertNotNull(result);
        assertEquals(result.get(0), institutionMapper.toInstitutionBase(userInfoResponse.getInstitutions().get(0)));
        verify(userApiRestClient, times(1))._usersUserIdInstitutionsGet(userId, null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()));
    }

    @Test
    void suspend() {

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";
        // when
        userConnector.suspendUserProduct(userId, institutionId, productId, productRole);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.SUSPENDED, productRole);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void activate() {

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        userConnector.activateUserProduct(userId, institutionId, productId, productRole);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.ACTIVE, productRole);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void delete() {

        // given
        String userId = "userId";
        String institutionId = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        userConnector.deleteUserProduct(userId, institutionId, productId, productRole);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.DELETED, productRole);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void getUserById() throws IOException{
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "institutionId";
        String field = "field";
        List<String> fields = List.of(field);

        ClassPathResource resource = new ClassPathResource("stubs/UserDetailResponse.json");
        UserDetailResponse userDetailResponse = objectMapper.readValue(Files.readAllBytes(resource.getFile().toPath()), new TypeReference<>() {
        });

        ClassPathResource resourceResponse = new ClassPathResource("stubs/User.json");
        User user = objectMapper.readValue(Files.readAllBytes(resourceResponse.getFile().toPath()), new TypeReference<>() {
        });

        when(userApiRestClient._usersIdDetailsGet(userId, field, institutionId)).thenReturn(ResponseEntity.ok(userDetailResponse));
        when(userMapper.toUser(userDetailResponse)).thenReturn(user);

        User result = userConnector.getUserById(userId, institutionId, fields);
        assertEquals(user,result);
        assertNotNull(result);
        verify(userApiRestClient, times(1))._usersIdDetailsGet(userId, field, institutionId);
    }
    @Test
    void getUserByIdEmptyUser() throws IOException{
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String institutionId = "institutionId";
        List<String> fields = Collections.emptyList();

        ClassPathResource resource = new ClassPathResource("stubs/UserDetailResponse.json");
        UserDetailResponse userDetailResponse = objectMapper.readValue(Files.readAllBytes(resource.getFile().toPath()), new TypeReference<>() {
        });

        when(userApiRestClient._usersIdDetailsGet(userId, null, institutionId)).thenReturn(ResponseEntity.ok(userDetailResponse));
        when(userMapper.toUser(userDetailResponse)).thenReturn(null);

        User result = userConnector.getUserById(userId, institutionId, fields);
        assertNull(result);
        verify(userApiRestClient, times(1))._usersIdDetailsGet(userId, null, institutionId);
    }
    @Test
    void verifyUserExist_UserExists() throws IOException {
        String userId = "userId";
        String institutionId = "institutionId";
        String productId = "productId";
        ClassPathResource resource = new ClassPathResource("stubs/UserInstitutionResponse2.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserInstitutionResponse userInstitutionResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, List.of(productId), null, List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name(), SUSPENDED.name()), userId)).thenReturn(ResponseEntity.ok(List.of(userInstitutionResponse)));

        List<UserInstitution> response = userConnector.retrieveFilteredUser(userId, institutionId, productId);
        assertEquals(institutionMapper.toInstitution(userInstitutionResponse), response.get(0));

        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(eq(institutionId), eq(null), eq(List.of(productId)), eq(null), anyList(), eq(userId));
    }

    @Test
    void verifyUserExist_UserDoesNotExist() throws IOException {
        String userId = "userId";
        String institutionId = "institutionId";
        String productId = "productId";
        ClassPathResource resource = new ClassPathResource("stubs/UserInstitutionResponse2.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserInstitutionResponse userInstitutionResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {});
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, List.of(productId), null, List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name(), SUSPENDED.name()), userId))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        List<UserInstitution> response = userConnector.retrieveFilteredUser(userId, institutionId, productId);
        assertEquals(0, response.size());

        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(eq(institutionId), eq(null), eq(List.of(productId)), eq(null), anyList(), eq(userId));
    }

    @Test
    void search() throws IOException {
        String fiscalCode = "fiscalCode";
        String institutionId = "institutionId";
        SearchUserDto searchUserDto = new SearchUserDto(fiscalCode);

        ClassPathResource resource = new ClassPathResource("stubs/UserDetailResponse.json");
        UserDetailResponse userDetailResponse = objectMapper.readValue(Files.readAllBytes(resource.getFile().toPath()), new TypeReference<>() {
        });

        ClassPathResource resourceResponse = new ClassPathResource("stubs/User.json");
        User user = objectMapper.readValue(Files.readAllBytes(resourceResponse.getFile().toPath()), new TypeReference<>() {
        });

        when(userApiRestClient._usersSearchPost(institutionId,searchUserDto)).thenReturn(ResponseEntity.ok(userDetailResponse));
        when(userMapper.toUser(userDetailResponse)).thenReturn(user);

        User result = userConnector.searchByFiscalCode(fiscalCode, institutionId);

        assertNotNull(result);
        assertEquals(user, result);
    }
    @Test
    void searchEmptyUser() throws IOException {
        String institutionId = "institutionId";
        SearchUserDto searchUserDto = new SearchUserDto();

        ClassPathResource resource = new ClassPathResource("stubs/UserDetailResponse.json");
        UserDetailResponse userDetailResponse = objectMapper.readValue(Files.readAllBytes(resource.getFile().toPath()), new TypeReference<>() {
        });


        when(userApiRestClient._usersSearchPost(institutionId,searchUserDto)).thenReturn(ResponseEntity.ok(userDetailResponse));
        when(userMapper.toUser(userDetailResponse)).thenReturn(null);

        User result = userConnector.searchByFiscalCode(null, institutionId);

        assertNull(result);
    }

    @Test
    void hasPermissionTrue() {
        UserInstitutionWithActions userInstitutionWithActions = new UserInstitutionWithActions();
        OnboardedProductWithActions onboardedProductWithActions = new OnboardedProductWithActions();
        onboardedProductWithActions.setUserProductActions(List.of("Selc:ViewBilling"));
        userInstitutionWithActions.setProducts(List.of(onboardedProductWithActions));

        //given
        String institutionId = "institutionId";
        String action = "Selc:ViewBilling";
        String productId = "productId";
        String userId = "userId";
        when(userApiRestClient._usersUserIdInstitutionsInstitutionIdGet(institutionId, userId, productId))
                .thenReturn(new ResponseEntity<>(userInstitutionWithActions, HttpStatus.OK));
        //when
        Boolean result = userConnector.hasPermission(userId, institutionId, productId, action);
        //then
        assertNotNull(result);
        assertEquals(true, result);
    }

    @Test
    void hasPermissionFalse() {
        UserInstitutionWithActions userInstitutionWithActions = new UserInstitutionWithActions();
        OnboardedProductWithActions onboardedProductWithActions = new OnboardedProductWithActions();
        onboardedProductWithActions.setUserProductActions(List.of("Selc:UpdateUser"));
        userInstitutionWithActions.setProducts(List.of(onboardedProductWithActions));
        //given
        String institutionId = "institutionId";
        String action = "Selc:ViewBilling";
        String productId = "productId";
        String userId = "userId";

        when(userApiRestClient._usersUserIdInstitutionsInstitutionIdGet(institutionId, userId, productId))
                .thenReturn(new ResponseEntity<>(userInstitutionWithActions, HttpStatus.OK));
        //when
        Boolean result = userConnector.hasPermission(userId, institutionId, productId, action);
        //then
        assertNotNull(result);
        assertEquals(false, result);
    }

    @Test
    void update() {
        // given
        final String userId = "userId";
        final String institutionId = "institutionId";
        final var user = new UpdateUserRequestDto();
        // when
        userConnector.updateUser(userId, institutionId, user);
        // then
        verify(userApiRestClient, times(1))
                ._usersIdUserRegistryPut(userId, institutionId, new UpdateUserRequest());
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void getUsers_emptyList() throws IOException {
        // given
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";

        ClassPathResource resourceFilter = new ClassPathResource("stubs/UserInfoFilter.json");
        UserInfo.UserInfoFilter userInfoFilter = objectMapper.readValue(Files.readAllBytes(resourceFilter.getFile().toPath()), new TypeReference<>() {
        });

        when(userApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, loggedUserId, userInfoFilter.getUserId(), userInfoFilter.getProductRoles(), List.of(userInfoFilter.getProductId()), List.of("MANAGER", "DELEGATE", "SUB_DELEGATE"), List.of("ACTIVE", "SUSPENDED")))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // when
        Collection<UserInfo> result = userConnector.getUsers(institutionId, userInfoFilter, loggedUserId);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsers_notEmpty() throws IOException {
        // given
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";

        ClassPathResource resourceFilter = new ClassPathResource("stubs/UserInfoFilter.json");
        UserInfo.UserInfoFilter userInfoFilter = objectMapper.readValue(Files.readAllBytes(resourceFilter.getFile().toPath()), new TypeReference<>() {});

        // Simulate response from API by reading from a JSON file
        ClassPathResource resourceResponse = new ClassPathResource("stubs/UserDataResponse.json");
        byte[] responseData = Files.readAllBytes(resourceResponse.getFile().toPath());
        List<UserDataResponse> userDataResponseList = objectMapper.readValue(responseData, new TypeReference<>() {});

        when(userApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, loggedUserId, userInfoFilter.getUserId(), userInfoFilter.getProductRoles(), List.of(userInfoFilter.getProductId()), List.of("MANAGER", "DELEGATE", "SUB_DELEGATE"), List.of("ACTIVE", "SUSPENDED")))
                .thenReturn(ResponseEntity.ok(userDataResponseList));

        // when
        Collection<UserInfo> result = userConnector.getUsers(institutionId, userInfoFilter, loggedUserId);

        // then
        assertEquals(1, result.size());
        UserInfo actualUserInfo = result.iterator().next();
        assertEquals(userMapper.toUserInfo(userDataResponseList.get(0)), actualUserInfo);
    }

    @Test
    void getProducts_returnsUserInstitution() throws IOException{
        String institutionId = "institutionId";
        String userId = "userId";


        ClassPathResource resource = new ClassPathResource("stubs/UserInstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserInstitutionResponse userInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(
                institutionId,
                null,
                null,
                null,
                null,
                userId
        )).thenReturn(ResponseEntity.ok(List.of(userInstitutions)));

        UserInstitution result = userConnector.getProducts(institutionId, userId);
        //Todo aggiungere expectedResponse

        assertNotNull(result);
        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, null, null, null, userId);
    }

    @Test
    void getProducts_throwsResourceNotFoundException() {
        String institutionId = "institutionId";
        String userId = "userId";
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(
                institutionId,
                null,
                null,
                null,
                null,
                userId
        )).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        Executable executable = () -> userConnector.getProducts(institutionId, userId);

        assertThrows(ResourceNotFoundException.class, executable);
        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, null, null, null, userId);
    }

    @Test
    void testCreateOrUpdateUserByFiscalCode() throws IOException {
        // Arrange
        when(userApiRestClient._usersPost(Mockito.any()))
                .thenReturn(ResponseEntity.ok("userId"));

        ClassPathResource resource = new ClassPathResource("stubs/UserToCreate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserToCreate userDto = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        CreateUserDto.Role role = new CreateUserDto.Role();
        role.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role.setProductRole("admin");

        CreateUserDto.Role role2 = new CreateUserDto.Role();
        role2.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role2.setProductRole("admin2");

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        ClassPathResource institutionResource = new ClassPathResource("stubs/Institution.json");
        byte[] institutionResourceStream = Files.readAllBytes(institutionResource.getFile().toPath());
        Institution institution = objectMapper.readValue(institutionResourceStream, new TypeReference<>() {});

        institution.setRootParent(response);
        // Act
        String userId = userConnector.createOrUpdateUserByFiscalCode(institution, "productId", userDto, List.of(role, role2));

        // Assert that nothing has changed
        verify(userApiRestClient)._usersPost(Mockito.any());
        assertEquals("userId", userId);
    }

    @Test
    void testCreateOrUpdateUserByFiscalCode_withNullRoles() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("stubs/UserToCreate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserToCreate userDto = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        ClassPathResource institutionResource = new ClassPathResource("stubs/Institution.json");
        byte[] institutionResourceStream = Files.readAllBytes(institutionResource.getFile().toPath());
        Institution institution = objectMapper.readValue(institutionResourceStream, new TypeReference<>() {});

        institution.setRootParent(response);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userConnector.createOrUpdateUserByFiscalCode(institution, "productId", userDto, null));

        assertEquals("Role list cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateOrUpdateUserByFiscalCode_withEmptyRoles() throws IOException {
        // Arrange
        ClassPathResource resource = new ClassPathResource("stubs/UserToCreate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserToCreate userDto = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        ClassPathResource institutionResource = new ClassPathResource("stubs/Institution.json");
        byte[] institutionResourceStream = Files.readAllBytes(institutionResource.getFile().toPath());
        Institution institution = objectMapper.readValue(institutionResourceStream, new TypeReference<>() {});

        institution.setRootParent(response);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userConnector.createOrUpdateUserByFiscalCode(institution, "productId", userDto, Collections.emptyList()));

        assertEquals("Role list cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateOrUpdateUserByFiscalCode_withApiResponseNull() throws IOException {
        // Arrange
        ClassPathResource userDtoResource = new ClassPathResource("stubs/CreateUserDto.json");
        byte[] userDtoResourceStream = Files.readAllBytes(userDtoResource.getFile().toPath());
        it.pagopa.selfcare.user.generated.openapi.v1.dto.CreateUserDto createUserDto = objectMapper.readValue(userDtoResourceStream, new TypeReference<>() {});
        when(userApiRestClient._usersPost(createUserDto))
                .thenReturn(ResponseEntity.ok(null));

        ClassPathResource resource = new ClassPathResource("stubs/UserToCreate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserToCreate userDto = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        CreateUserDto.Role role = new CreateUserDto.Role();
        role.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role.setProductRole("admin");

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        ClassPathResource institutionResource = new ClassPathResource("stubs/Institution.json");
        byte[] institutionResourceStream = Files.readAllBytes(institutionResource.getFile().toPath());
        Institution institution = objectMapper.readValue(institutionResourceStream, new TypeReference<>() {});

        institution.setRootParent(response);

        // Act
        String userId = userConnector.createOrUpdateUserByFiscalCode(institution, "productId", userDto, List.of(role));

        // Assert that nothing has changed
        verify(userApiRestClient)._usersPost(createUserDto);
        assertNull(userId);
    }

    @Test
    void testCreateOrUpdateUserByFiscalCode_withApiResponseEmpty() throws IOException {
        // Arrange
        ClassPathResource userDtoResource = new ClassPathResource("stubs/CreateUserDto.json");
        byte[] userDtoResourceStream = Files.readAllBytes(userDtoResource.getFile().toPath());
        it.pagopa.selfcare.user.generated.openapi.v1.dto.CreateUserDto createUserDto = objectMapper.readValue(userDtoResourceStream, new TypeReference<>() {});
        when(userApiRestClient._usersPost(createUserDto))
                .thenReturn(ResponseEntity.ok(""));

        ClassPathResource resource = new ClassPathResource("stubs/UserToCreate.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserToCreate userDto = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        CreateUserDto.Role role = new CreateUserDto.Role();
        role.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role.setProductRole("admin");

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        ClassPathResource institutionResource = new ClassPathResource("stubs/Institution.json");
        byte[] institutionResourceStream = Files.readAllBytes(institutionResource.getFile().toPath());
        Institution institution = objectMapper.readValue(institutionResourceStream, new TypeReference<>() {});

        institution.setRootParent(response);

        // Act
        String userId = userConnector.createOrUpdateUserByFiscalCode(institution, "productId", userDto, List.of(role));

        // Assert that nothing has changed
        verify(userApiRestClient)._usersPost(createUserDto);
        assertEquals("", userId);
    }
    @Test
    void testCreateOrUpdateUserByUserId() throws IOException {
        ClassPathResource userRoleResource = new ClassPathResource("stubs/AddUserRoleDto.json");
        byte[] userRoleResourceStream = Files.readAllBytes(userRoleResource.getFile().toPath());
        AddUserRoleDto addUserRoleDto = objectMapper.readValue(userRoleResourceStream, new TypeReference<>() {});

        // Arrange
        when(userApiRestClient._usersUserIdPost("userId", addUserRoleDto))
                .thenReturn(ResponseEntity.ok().build());

        CreateUserDto.Role role = new CreateUserDto.Role();
        role.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role.setProductRole("admin");

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");

        ClassPathResource resource = new ClassPathResource("stubs/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        institution.setRootParent(response);

        // Act
        userConnector.createOrUpdateUserByUserId(institution, "productId", "userId", List.of(role));

        // Assert that nothing has changed
        verify(userApiRestClient)._usersUserIdPost("userId", addUserRoleDto);
    }


    @Test
    void retrieveFilteredUserInstitutionReturnsUserInstitutions() throws IOException {
        // Given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        userInfoFilter.setProductId("productId");
        ClassPathResource resource = new ClassPathResource("stubs/UserInstitutionResponse.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        UserInstitutionResponse userInstitutionResponse = objectMapper.readValue(resourceStream, new TypeReference<>() {});

        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, List.of(userInfoFilter.getProductId()), null,
                userInfoFilter.getAllowedStates().stream().map(Enum::name).toList(), null))
                .thenReturn(ResponseEntity.ok(List.of(userInstitutionResponse)));

        // When
        List<String> result = userConnector.retrieveFilteredUserInstitution(institutionId, userInfoFilter);

        // Then
        assertEquals(userInstitutionResponse.getUserId(), result.get(0));
    }

    @Test
    void retrieveFilteredUserInstitutionReturnsEmptyListWhenNoUserInstitutions() {
        // Given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId("productId");
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, List.of(userInfoFilter.getProductId()), null,
                userInfoFilter.getAllowedStates().stream().map(Enum::name).toList(), null))
                .thenReturn(ResponseEntity.ok(List.of()));

        // When
        List<String> result = userConnector.retrieveFilteredUserInstitution(institutionId, userInfoFilter);

        // Then
        assertEquals(0, result.size());
    }

}
