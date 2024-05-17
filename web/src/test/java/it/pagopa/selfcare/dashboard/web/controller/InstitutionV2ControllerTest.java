package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserToCreate;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.core.InstitutionV2Service;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2Impl;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {InstitutionV2Controller.class, InstitutionResourceMapperImpl.class, UserMapperV2Impl.class})
@WebMvcTest(value = {InstitutionV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class InstitutionV2ControllerTest {

    private static final String BASE_URL = "/v2/institutions";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserV2Service userServiceMock;

    @Autowired
    private InstitutionV2Controller institutionV2Controller;

    @MockBean
    private InstitutionV2Service institutionV2ServiceMock;

    @MockBean
    DelegationService delegationServiceMock;

    /**
     * Method under test: {@link InstitutionV2Controller#getInstitutionUser(String, String, Authentication)}
     */
    @Test
    void getInstitutionUser_notNullUser() throws Exception {
        //given
        final String institutionId = "institutionId";
        final String userId = "notFound";
        UserInfo userInfo = mockInstance(new UserInfo(), "setId");
        userInfo.setId(randomUUID().toString());

        String loggedUserId = "loggedUserId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(loggedUserId).build());

        when(institutionV2ServiceMock.getInstitutionUser(any(), any(), any()))
                .thenReturn(userInfo);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        InstitutionUserResource userResource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionUserResource.class);
        assertNotNull(userResource);
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

        InstitutionBase expectedInstitution = mockInstance(new InstitutionBase());
        expectedInstitution.setUserRole("MANAGER");
        List<InstitutionBase> expectedInstitutionInfos = new ArrayList<>();
        expectedInstitutionInfos.add(expectedInstitution);
        when(userServiceMock.getInstitutions(userId)).thenReturn(expectedInstitutionInfos);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(resources);
        assertFalse(resources.isEmpty());
        assertEquals(resources.get(0).getStatus(), expectedInstitution.getStatus());
        assertNotNull(resources.get(0).getUserRole());
        verify(userServiceMock, times(1))
                .getInstitutions(userId);
        verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void getInstitutionTest() throws Exception {
        //given
        final String institutionId = "institutionId";
        UserInfo userInfo = mockInstance(new UserInfo(), "setId");
        userInfo.setId(randomUUID().toString());

        Institution institution = new Institution();
        institution.setId(institutionId);

        String loggedUserId = "loggedUserId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(loggedUserId).build());

        when(institutionV2ServiceMock.findInstitutionById(any()))
                .thenReturn(institution);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}", institutionId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        InstitutionResource userResource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionResource.class);
        assertNotNull(userResource);
        verify(institutionV2ServiceMock, times(1))
                .findInstitutionById(institutionId);
        verifyNoMoreInteractions(institutionV2ServiceMock);

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
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());

        // when
        mvc.perform(MockMvcRequestBuilders
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
        UserProductRoles userProductRoles = null;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder(userId).build());

        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/{institutionId}/products/{productId}/users/{userId}", institutionId, productId, userId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(userProductRoles))
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

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("userId").build());
        when(userServiceMock.createUsers(eq(institutionId), eq(productId), any(UserToCreate.class))).thenReturn(UUID.randomUUID().toString());
        // when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto))
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        verify(userServiceMock, times(1))
                .createUsers(eq(institutionId), eq(productId), any(UserToCreate.class));
        verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void createInstitutionProductUser_whenCreateUserDtoIsNull() throws Exception {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        CreateUserDto createUserDto = null;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("userId").build());

        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .principal(authentication)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(createUserDto))
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

        when(delegationServiceMock.getDelegationsV2(any())).thenReturn(expectedDelegationWithPagination);
        // When

        MvcResult result = mvc
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
        GetDelegationParameters delegationParameters = createDelegationParameters(null, "to", "prod-id", "taxCode", "search", GetDelegationsMode.FULL, Order.ASC, 0, 10);
        DelegationWithPagination expectedDelegationWithPagination = new DelegationWithPagination(List.of(expectedDelegation), exptectedPageInfo);

        when(delegationServiceMock.getDelegationsV2(any())).thenReturn(expectedDelegationWithPagination);
        // When

        MvcResult result = mvc
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

    private DelegationWithInfo createDelegation(String pattern, String from, String to) {
        DelegationWithInfo delegation = new DelegationWithInfo();
        delegation.setId("id_" + pattern);
        delegation.setProductId("productId");
        delegation.setType(DelegationType.PT);
        delegation.setInstitutionId(to);
        delegation.setBrokerId(from);
        delegation.setInstitutionName("name_" + from);
        delegation.setBrokerName("name_" + to);
        return delegation;
    }

    private GetDelegationParameters createDelegationParameters(String from, String to, String productId,
                                                               String search, String taxCode, GetDelegationsMode mode,
                                                               Order order, Integer page, Integer size) {
        return GetDelegationParameters.builder()
                .from(from)
                .to(to)
                .productId(productId)
                .search(search)
                .taxCode(taxCode)
                .mode(mode.name())
                .order(order.name())
                .page(page)
                .size(size)
                .build();
    }
}
