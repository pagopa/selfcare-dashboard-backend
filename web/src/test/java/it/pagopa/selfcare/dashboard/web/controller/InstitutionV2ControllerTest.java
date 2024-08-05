package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserToCreate;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.core.InstitutionV2Service;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2Impl;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InstitutionV2ControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/v2/institutions";
    private static final String FILE_JSON_PATH = "src/test/resources/json/";

    @InjectMocks
    private InstitutionV2Controller institutionV2Controller;
    @Mock
    private UserV2Service userServiceMock;
    @Mock
    private InstitutionV2Service institutionV2ServiceMock;
    @Mock
    DelegationService delegationServiceMock;
    @Spy
    private UserMapperV2Impl userMapper;
    @Spy
    private InstitutionResourceMapperImpl institutionResourceMapper;
    @BeforeEach
    void setUp() {
        super.setUp(institutionV2Controller);
    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitutionUser(String, String, Authentication)}
     */
    @Test
    void getInstitutionUser_notNullUser() throws Exception {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";

        byte[] userInfoStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserInfo.json"));
        UserInfo userInfo = objectMapper.readValue(userInfoStream, UserInfo.class);

        String loggedUserId = "loggedUserId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(loggedUserId).build());

        when(institutionV2ServiceMock.getInstitutionUser(institutionId, userId, loggedUserId))
                .thenReturn(userInfo);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "InstitutionUserDetailsResource.json")))))
                .andReturn();
        //then
        verify(institutionV2ServiceMock, times(1))
                .getInstitutionUser(institutionId, userId, loggedUserId);
        verifyNoMoreInteractions(institutionV2ServiceMock);

    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitutions(Authentication)}
     */
    @Test
    void getInstitutions_institutionInfoNotNull() throws Exception {
        // given
        final String userId = "userId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());

        InstitutionBase expectedInstitution = getInstitutionBase();
        List<InstitutionBase> expectedInstitutionInfos = new ArrayList<>();
        expectedInstitutionInfos.add(expectedInstitution);
        when(userServiceMock.getInstitutions(userId)).thenReturn(expectedInstitutionInfos);
        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        List<InstitutionBaseResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(resources);
        assertEquals(resources.get(0).getId(), expectedInstitution.getId());
        assertEquals("ADMIN", resources.get(0).getUserRole());
        assertEquals(resources.get(0).getName(), expectedInstitution.getName());
        assertEquals(resources.get(0).getStatus(), expectedInstitution.getStatus());
        assertEquals(resources.get(0).getParentDescription(), expectedInstitution.getParentDescription());

        verify(userServiceMock, times(1))
                .getInstitutions(userId);
        verifyNoMoreInteractions(userServiceMock);
    }

    private InstitutionBase getInstitutionBase() {
        InstitutionBase expectedInstitution = mockInstance(new InstitutionBase());
        expectedInstitution.setId("123456");
        expectedInstitution.setUserRole("MANAGER");
        expectedInstitution.setName("name");
        expectedInstitution.setStatus("status");
        expectedInstitution.setParentDescription("parentDescription");
        return expectedInstitution;
    }

    @Test
    void getInstitution_institutionInfoNotNull() throws Exception {
        String institutionId = "institutionId";
        byte[] institutionStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "Institution.json"));
        Institution institution = objectMapper.readValue(institutionStream, Institution.class);

        when(institutionV2ServiceMock.findInstitutionById(institutionId)).thenReturn(institution);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "InstitutionResource.json")))))
                .andReturn();
    }

    @Test
    void addUserProductRoles_happyPath() throws Exception {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";
        UserProductRoles userProductRoles = new UserProductRoles();
        userProductRoles.setProductRoles(Set.of("admin"));

        Authentication authentication = mock(Authentication.class);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/{institutionId}/products/{productId}/users/{userId}", institutionId, productId, userId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(userProductRoles))
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        verify(userServiceMock, times(1))
                .addUserProductRoles(institutionId, productId, userId, userProductRoles.getProductRoles());
        verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void addUserProductRoles_whenUserProductRolesIsNull() throws Exception {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";

        Authentication authentication = mock(Authentication.class);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/{institutionId}/products/{productId}/users/{userId}", institutionId, productId, userId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(null))
                        .accept(APPLICATION_JSON_VALUE))
                .andReturn();

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        verifyNoInteractions(userServiceMock);
    }

    @Test
    void createInstitutionProductUser_happyPath() throws Exception {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName("John");
        createUserDto.setSurname("Doe");
        createUserDto.setTaxCode("ABC123");
        createUserDto.setEmail("john.doe@example.com");
        createUserDto.setProductRoles(Set.of("admin"));

        UserToCreate userToCreate = userMapper.toUserToCreate(createUserDto);

        String id = UUID.randomUUID().toString();

        Authentication authentication = mock(Authentication.class);
        when(userServiceMock.createUsers(institutionId, productId, userToCreate)).thenReturn(id);
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto))
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id)))
                .andReturn();

        // then
        verify(userServiceMock, times(1))
                .createUsers(institutionId, productId, userToCreate);
        verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void createInstitutionProductUser_whenCreateUserDtoIsNull() throws Exception {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";

        Authentication authentication = mock(Authentication.class);
        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(null))
                        .accept(APPLICATION_JSON_VALUE))
                .andReturn();

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        verifyNoInteractions(userServiceMock);
    }

    @Test
    void getDelegationsUsingTo_shouldGetDataWithoutFilters() throws Exception {
        // Given
        DelegationWithInfo expectedDelegation = dummyDelegation();
        PageInfo exptectedPageInfo = new PageInfo(10000, 0, 1, 1);

        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .to("to")
                .build();

        DelegationWithPagination expectedDelegationWithPagination = new DelegationWithPagination(List.of(expectedDelegation), exptectedPageInfo);

        when(delegationServiceMock.getDelegationsV2(delegationParameters)).thenReturn(expectedDelegationWithPagination);
        // When

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/institutions", expectedDelegation.getBrokerId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        DelegationWithPagination resource = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        // Then
        AssertionsForClassTypes.assertThat(resource).isNotNull();
        org.assertj.core.api.Assertions.assertThat(resource.getDelegations()).hasSize(1);
        DelegationWithInfo actual = resource.getDelegations().get(0);
        AssertionsForClassTypes.assertThat(actual.getId()).isEqualTo(expectedDelegation.getId());
        AssertionsForClassTypes.assertThat(actual.getInstitutionName()).isEqualTo(expectedDelegation.getInstitutionName());
        AssertionsForClassTypes.assertThat(actual.getInstitutionRootName()).isEqualTo(expectedDelegation.getInstitutionRootName());
        AssertionsForClassTypes.assertThat(actual.getBrokerName()).isEqualTo(expectedDelegation.getBrokerName());
        AssertionsForClassTypes.assertThat(actual.getBrokerId()).isEqualTo(expectedDelegation.getBrokerId());
        AssertionsForClassTypes.assertThat(actual.getProductId()).isEqualTo(expectedDelegation.getProductId());
        AssertionsForClassTypes.assertThat(actual.getInstitutionId()).isEqualTo(expectedDelegation.getInstitutionId());

        verify(delegationServiceMock, times(1))
                .getDelegationsV2(delegationParameters);
        verifyNoMoreInteractions(delegationServiceMock);
    }

    @Test
    void getDelegationsUsingTo_shouldGetData() throws Exception {
        // Given
        DelegationWithInfo expectedDelegation = dummyDelegation();
        PageInfo exptectedPageInfo = new PageInfo(10, 0, 1, 1);
        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .from(null)
                .to("to")
                .productId("prod-id")
                .search("search")
                .order(Order.ASC.name())
                .page(0)
                .size(10)
                .build();
        DelegationWithPagination expectedDelegationWithPagination = new DelegationWithPagination(List.of(expectedDelegation), exptectedPageInfo);

        when(delegationServiceMock.getDelegationsV2(delegationParameters)).thenReturn(expectedDelegationWithPagination);
        // When

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/institutions?productId={productId}&search={search}&taxCode={taxCode}&mode=FULL&order=ASC&page={page}&size={size}",
                                delegationParameters.getTo(), delegationParameters.getProductId(), delegationParameters.getSearch(),
                                delegationParameters.getTaxCode(), delegationParameters.getPage(), delegationParameters.getSize()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        DelegationWithPagination resource = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        // Then
        AssertionsForClassTypes.assertThat(resource).isNotNull();
        org.assertj.core.api.Assertions.assertThat(resource.getDelegations()).hasSize(1);
        DelegationWithInfo actual = resource.getDelegations().get(0);
        AssertionsForClassTypes.assertThat(actual.getId()).isEqualTo(expectedDelegation.getId());
        AssertionsForClassTypes.assertThat(actual.getInstitutionName()).isEqualTo(expectedDelegation.getInstitutionName());
        AssertionsForClassTypes.assertThat(actual.getBrokerName()).isEqualTo(expectedDelegation.getBrokerName());
        AssertionsForClassTypes.assertThat(actual.getBrokerId()).isEqualTo(expectedDelegation.getBrokerId());
        AssertionsForClassTypes.assertThat(actual.getProductId()).isEqualTo(expectedDelegation.getProductId());
        AssertionsForClassTypes.assertThat(actual.getInstitutionId()).isEqualTo(expectedDelegation.getInstitutionId());

        verify(delegationServiceMock, times(1))
                .getDelegationsV2(delegationParameters);
        verifyNoMoreInteractions(delegationServiceMock);
    }

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitutionOnboardingPending(String, String)}
     */
    @Test
    void getInstitutionOnboarding204() throws Exception {
        //given
        final String institutionId = "institutionId";
        final String productId = "productId";

        doReturn(Boolean.FALSE).when(institutionV2ServiceMock).verifyIfExistsPendingOnboarding(institutionId, productId);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/onboardings/{productId}/pending", institutionId, productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        verify(institutionV2ServiceMock, times(1))
                .verifyIfExistsPendingOnboarding(institutionId, productId);
        verifyNoMoreInteractions(institutionV2ServiceMock);

    }

    @Test
    void getInstitutionOnboarding200() throws Exception {
        //given
        final String institutionId = "institutionId";
        final String productId = "productId";

        doReturn(Boolean.TRUE).when(institutionV2ServiceMock).verifyIfExistsPendingOnboarding(institutionId, productId);
        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/onboardings/{productId}/pending", institutionId, productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        verify(institutionV2ServiceMock, times(1))
                .verifyIfExistsPendingOnboarding(institutionId, productId);
        verifyNoMoreInteractions(institutionV2ServiceMock);

    }


    private DelegationWithInfo dummyDelegation() {
        DelegationWithInfo delegation = new DelegationWithInfo();
        delegation.setInstitutionId("from");
        delegation.setBrokerId("to");
        delegation.setId("setId");
        delegation.setProductId("setProductId");
        delegation.setType(DelegationType.PT);
        delegation.setInstitutionName("setInstitutionFromName");
        delegation.setBrokerName("setInstitutionFromRootName");
        return delegation;
    }
}
