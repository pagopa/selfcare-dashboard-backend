package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2Impl;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {UserV2Controller.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserV2Controller.class, InstitutionResourceMapperImpl.class, WebTestConfig.class, UserMapperV2Impl.class})
class UserV2ControllerTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private UserV2Service userServiceMock;


    @Autowired
    protected ObjectMapper objectMapper;

    @Spy
    UserMapperV2 userMapperV2 = new UserMapperV2Impl();

    private static final String BASE_URL = "/v2/users";

    private static final User USER_RESOURCE;

    static {
        USER_RESOURCE = mockInstance(new User());
        USER_RESOURCE.setId(randomUUID().toString());
        Map<String, WorkContact> workContacts = new HashMap<>();
        WorkContact workContact = mockInstance(new WorkContact());
        workContact.getEmail().setCertification(Certification.SPID);
        workContacts.put("institutionId", workContact);
        USER_RESOURCE.setWorkContacts(workContacts);
    }

    @Test
    void suspendRelationship() throws Exception {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{userId}/suspend", userId)
                        .queryParam("institutionId", institutionid )
                        .queryParam("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .suspendUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }


    @Test
    void activateRelationship() throws Exception {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{userId}/activate", userId)
                        .queryParam("institutionId", institutionid )
                        .queryParam("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .activateUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void deleteRelationship() throws Exception {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .delete(BASE_URL + "/{userId}", userId)
                        .queryParam("institutionId", institutionid )
                        .queryParam("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .deleteUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void getUserById() throws Exception {
        //given
        final String userId = "userId";
        final String institutionId = "institutionId";
        final List<String> fields = List.of("fields");
        when(userServiceMock.getUserById(anyString(), anyString(), any())).thenReturn(USER_RESOURCE);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL+"/{id}", userId)
                        .param("institutionId", institutionId)
                        .param("fields", fields.get(0))
                .contentType(APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        //then
        UserResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getId());
        verify(userServiceMock, times(1)).getUserById(userId, institutionId, fields);

    }

    @Test
    void search() throws Exception {
        //given
        final String externalId = "externalId";
        final String institutionId = "institutionId";
        SearchUserDto externalIdDto = new SearchUserDto();
        externalIdDto.setFiscalCode(externalId);
        Mockito.when(userServiceMock.searchUserByFiscalCode(anyString(), anyString()))
                .thenReturn(USER_RESOURCE);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/search")
                        .param("institutionId", institutionId)
                        .content(objectMapper.writeValueAsString(externalIdDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        //then
        UserResource userResponse = objectMapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        assertNotNull(userResponse);
        Mockito.verify(userServiceMock, Mockito.times(1))
                .searchUserByFiscalCode(externalId, institutionId);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    /**
     * Method under test: {@link UserV2Controller#updateUser(String, String, UpdateUserDto)}
     */
    @Test
    void updateUser(@Value("classpath:stubs/updateUserDto.json") Resource updateUserDto) throws Exception {
        //given
        final String id = "userId";
        final String institutionId = "institutionId";
        //when
        mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/{id}", id)
                        .queryParam("institutionId", institutionId)
                        .content(updateUserDto.getInputStream().readAllBytes())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));
        //then
        verify(userServiceMock, times(1))
                .updateUser(eq(id), eq(institutionId), any(UpdateUserRequestDto.class));

        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void getUsers_institutionIdProductIdValid() throws Exception {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(SelfCareUser.builder("userId").build());

        UserInfo userInfo = mockInstance(new UserInfo());
        userInfo.setId(randomUUID().toString());
        List<UserInfo> userInfos = List.of(userInfo);

        when(userServiceMock.getUsersByInstitutionId(institutionId, productId, null, "userId")).thenReturn(userInfos);

        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/institution/" + institutionId)
                        .principal(authentication)
                        .queryParam("productId", productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<ProductUserResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(resources);
        assertFalse(resources.isEmpty());
        verify(userServiceMock, times(1))
                .getUsersByInstitutionId(institutionId, productId, null, "userId");
        verifyNoMoreInteractions(userServiceMock);
    }


}
