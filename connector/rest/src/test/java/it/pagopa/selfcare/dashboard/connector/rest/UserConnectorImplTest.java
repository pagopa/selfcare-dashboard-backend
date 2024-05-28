package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {UserConnectorImpl.class, InstitutionMapperImpl.class, UserMapper.class})
class UserConnectorImplTest {

    @Mock
    UserApiRestClient userApiRestClient;

    @Mock
    UserPermissionRestClient userPermissionRestClient;

    @Mock
    UserInstitutionApiRestClient userInstitutionApiRestClient;


    UserConnectorImpl userConnector;

    @Spy
    UserMapper userMapper = new UserMapperImpl();

    @BeforeEach
    void setup() {
        userConnector = new UserConnectorImpl(userApiRestClient, userInstitutionApiRestClient, userPermissionRestClient, new InstitutionMapperImpl(), userMapper);
    }


    @Test
    void getUserProductsNotFound() {
        when(userApiRestClient._usersUserIdInstitutionsGet("userID", null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> userConnector.getUserInstitutions("userID"));
    }

    @Test
    void getUserProductsFound() {
        UserInfoResponse userProductsResponse = getUserProductsResponse();
        when(userApiRestClient._usersUserIdInstitutionsGet("userID", null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name()))).thenReturn(ResponseEntity.ok(userProductsResponse));
        List<InstitutionBase> result = userConnector.getUserInstitutions("userID");
        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals(ACTIVE.name(), result.get(0).getStatus());
        Assertions.assertEquals("a", result.get(0).getName());
        Assertions.assertEquals("b", result.get(1).getName());
        Assertions.assertEquals("c", result.get(2).getName());
        Assertions.assertNull(result.get(3).getName());
    }

    private static UserInfoResponse getUserProductsResponse() {
        UserInfoResponse userProductsResponse = new UserInfoResponse();
        userProductsResponse.setUserId("userId");
        UserInstitutionRoleResponse institutionProducts = new UserInstitutionRoleResponse();
        institutionProducts.setInstitutionId("institutionId");
        institutionProducts.setInstitutionName("c");
        institutionProducts.setStatus(OnboardedProductState.ACTIVE);
        institutionProducts.setRole(PartyRole.MANAGER);

        UserInstitutionRoleResponse institutionProducts2 = new UserInstitutionRoleResponse();
        institutionProducts2.setInstitutionId("institutionId2");
        institutionProducts2.setInstitutionName("b");
        institutionProducts2.setStatus(OnboardedProductState.ACTIVE);
        institutionProducts2.setRole(PartyRole.MANAGER);

        UserInstitutionRoleResponse institutionProducts4 = new UserInstitutionRoleResponse();
        institutionProducts4.setInstitutionId("institutionId3");
        institutionProducts4.setStatus(OnboardedProductState.ACTIVE);
        institutionProducts4.setRole(PartyRole.MANAGER);

        UserInstitutionRoleResponse institutionProducts3 = new UserInstitutionRoleResponse();
        institutionProducts3.setInstitutionId("institutionId3");
        institutionProducts3.setInstitutionName("a");
        institutionProducts3.setStatus(OnboardedProductState.ACTIVE);
        institutionProducts3.setRole(PartyRole.MANAGER);



        userProductsResponse.setInstitutions(List.of(institutionProducts, institutionProducts2,institutionProducts4, institutionProducts3));
        return userProductsResponse;
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
    void getUserById() {

        //given
        final String userId = "userId";
        final String institutionId = "institutionId";
        final List<String> fields = List.of("fields");
        UserDetailResponse userDetailResponse = mockInstance(new UserDetailResponse());
        when(userApiRestClient._usersIdDetailsGet(anyString(), anyString(), anyString())).thenReturn(new ResponseEntity<>(userDetailResponse, HttpStatus.OK));
        //when
        User user = userConnector.getUserById(userId, institutionId, fields);
        //then
        assertNotNull(user);
        verify(userApiRestClient, times(1))._usersIdDetailsGet(userId, fields.get(0), institutionId);
    }
    @Test
    void verifyUserExist_UserExists() {
        String userId = "userId";
        String institutionId = "institutionId";
        String productId = "productId";
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(eq(institutionId), eq(null), eq(List.of(productId)), eq(null), anyList(), eq(userId))).thenReturn(ResponseEntity.ok(List.of(new UserInstitutionResponse())));

       List<UserInstitution> response = userConnector.retrieveFilteredUser(userId, institutionId, productId);
       assertEquals(1, response.size());

        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(eq(institutionId), eq(null), eq(List.of(productId)), eq(null), anyList(), eq(userId));
    }

    @Test
    void verifyUserExist_UserDoesNotExist() {
        String userId = "userId";
        String institutionId = "institutionId";
        String productId = "productId";
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(eq(institutionId), eq(null), eq(List.of(productId)), eq(null), anyList(), eq(userId)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        List<UserInstitution> response = userConnector.retrieveFilteredUser(userId, institutionId, productId);
        assertEquals(0, response.size());

        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(eq(institutionId), eq(null), eq(List.of(productId)), eq(null), anyList(), eq(userId));
    }

    @Test
    void search() {
        //given
        String fiscalCode = "fiscalCode";
        final String institutionId = "institutionId";
        UserDetailResponse userDetailResponse = mockInstance(new UserDetailResponse());
        when(userApiRestClient._usersSearchPost(any(), any())).thenReturn(new ResponseEntity<>(userDetailResponse, HttpStatus.OK));
        //when
        User user = userConnector.searchByFiscalCode(fiscalCode, institutionId);
        //then
        assertNotNull(user);
        ArgumentCaptor<SearchUserDto> searchUserDtoArgumentCaptor = ArgumentCaptor.forClass(SearchUserDto.class);
        verify(userApiRestClient, times(1))._usersSearchPost(eq(institutionId), searchUserDtoArgumentCaptor.capture());
        SearchUserDto captured = searchUserDtoArgumentCaptor.getValue();
        assertEquals(fiscalCode, captured.getFiscalCode());
    }

    @Test
    void hasPermissionTrue() {
        //given
        String institutionId = "institutionId";
        String permission = "ADMIN";
        String productId = "productId";
        when(userPermissionRestClient._authorizeGet(PermissionTypeEnum.ADMIN, institutionId, productId)).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));
        //when
        Boolean result = userConnector.hasPermission(institutionId, permission, productId);
        //then
        assertNotNull(result);
        assertEquals(true, result);
    }

    @Test
    void hasPermissionFalse() {
        //given
        String institutionId = "institutionId";
        String permission = "ADMIN";
        String productId = "productId";
        when(userPermissionRestClient._authorizeGet(PermissionTypeEnum.ADMIN, institutionId, productId)).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));
        //when
        Boolean result = userConnector.hasPermission(institutionId, permission, productId);
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
                ._usersIdUserRegistryPut(userId, institutionId,new UpdateUserRequest());
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void getUsers_emptyList() {
        // given
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        userInfoFilter.setRole(SelfCareAuthority.ADMIN);

        when(userApiRestClient._usersUserIdInstitutionInstitutionIdGet(eq(institutionId),  eq(loggedUserId), eq(null), eq(null), eq(null),eq(List.of("MANAGER", "DELEGATE", "SUB_DELEGATE")), eq(List.of("ACTIVE", "SUSPENDED"))))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // when
        Collection<UserInfo> result = userConnector.getUsers(institutionId, userInfoFilter, loggedUserId);

        // then
        assertTrue(result.isEmpty());
        verify(userApiRestClient, times(1))._usersUserIdInstitutionInstitutionIdGet(eq(institutionId),  eq(loggedUserId), eq(null), eq(null), eq(null),eq(List.of("MANAGER", "DELEGATE", "SUB_DELEGATE")), eq(List.of("ACTIVE", "SUSPENDED")));
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void getUsers_notEmpty() {
        // given
        String institutionId = "institutionId";
        String loggedUserId = "loggedUserId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        when(userApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId, loggedUserId, null, null, null, null, null))
                .thenReturn(ResponseEntity.ok(List.of(UserDataResponse.builder().userId("userId").build())));

        when(userMapper.toUserInfo(any())).thenReturn(new UserInfo());

        // when
        Collection<UserInfo> result = userConnector.getUsers(institutionId, userInfoFilter, loggedUserId);

        // then
        assertEquals(1, result.size());
        verify(userApiRestClient, times(1))._usersUserIdInstitutionInstitutionIdGet(institutionId, loggedUserId, null, null, null, null, null);
        verifyNoMoreInteractions(userApiRestClient);
    }

    @Test
    void getProducts_returnsUserInstitution() {
        // given
        String institutionId = "institutionId";
        String userId = "userId";
        UserInstitutionResponse userInstitutionResponse = new UserInstitutionResponse();
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(
                eq(institutionId),
                any(),
                any(),
                any(),
                any(),
                eq(userId)
        )).thenReturn(ResponseEntity.ok(List.of(userInstitutionResponse)));

        // when
        UserInstitution result = userConnector.getProducts(institutionId, userId);

        // then
        assertNotNull(result);
        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, null, null, null, userId);
    }

    @Test
    void getProducts_throwsResourceNotFoundException() {
        // given
        String institutionId = "institutionId";
        String userId = "userId";
        when(userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(
                eq(institutionId),
                any(),
                any(),
                any(),
                any(),
                eq(userId)
        )).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // when
        Executable executable = () -> userConnector.getProducts(institutionId, userId);

        // then
        assertThrows(ResourceNotFoundException.class, executable);
        verify(userInstitutionApiRestClient, times(1))._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, null, null, null, userId);
    }

    @Test
    void testCreateOrUpdateUserByFiscalCode() {
        // Arrange
        when(userApiRestClient._usersPost(Mockito.any()))
                .thenReturn(ResponseEntity.ok("userId"));

        UserToCreate userDto = new UserToCreate();
        userDto.setName("Name");
        userDto.setProductRoles(new HashSet<>());
        userDto.setSurname("Doe");
        userDto.setTaxCode("Tax Code");
        userDto.setEmail("jane.doe@example.org");

        CreateUserDto.Role role = new CreateUserDto.Role();
        role.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role.setProductRole("admin");

        CreateUserDto.Role role2 = new CreateUserDto.Role();
        role2.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role2.setProductRole("admin2");

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        Institution institution = new Institution();
        institution.setId("id");
        institution.setDescription("description");
        institution.setRootParent(response);
        // Act
        userConnector.createOrUpdateUserByFiscalCode(institution,  "productId",  userDto, List.of(role, role2));

        // Assert that nothing has changed
        verify(userApiRestClient)._usersPost(Mockito.any());
        assertEquals("Doe", userDto.getSurname());
        assertEquals("Name", userDto.getName());
        assertEquals("Tax Code", userDto.getTaxCode());
        assertEquals("jane.doe@example.org", userDto.getEmail());
    }

    @Test
    void testCreateOrUpdateUserByUserId() {
        // Arrange
        when(userApiRestClient._usersUserIdPost(eq("userId"), any()))
                .thenReturn(ResponseEntity.ok().build());

        CreateUserDto.Role role = new CreateUserDto.Role();
        role.setPartyRole(it.pagopa.selfcare.onboarding.common.PartyRole.MANAGER);
        role.setProductRole("admin");

        RootParentResponse response = new RootParentResponse();
        response.setDescription("rootDescription");
        Institution institution = new Institution();
        institution.setId("id");
        institution.setDescription("description");
        institution.setRootParent(response);

        // Act
        userConnector.createOrUpdateUserByUserId(institution, "productId", "userId", List.of(role));

        // Assert that nothing has changed
        verify(userApiRestClient)._usersUserIdPost(eq("userId"), any());
    }

}
