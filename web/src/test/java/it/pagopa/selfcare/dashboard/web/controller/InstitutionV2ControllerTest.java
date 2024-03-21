package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserToCreate;
import it.pagopa.selfcare.dashboard.core.InstitutionV2Service;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2Impl;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
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
}
