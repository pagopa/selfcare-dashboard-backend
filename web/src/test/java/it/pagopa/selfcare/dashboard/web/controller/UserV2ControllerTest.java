package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.user.UpdateUserRequestDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2Impl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final String BASE_URL = "/v2/users";

    private static final String FILE_JSON_PATH = "src/test/resources/json/";

    @Test
    void suspendRelationship() throws Exception {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{userId}/suspend", userId)
                        .queryParam("institutionId", institutionid)
                        .queryParam("productId", productId)
                        .queryParam("productRole", productRole)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .suspendUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }


    @Test
    void activateRelationship() throws Exception {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{userId}/activate", userId)
                        .queryParam("institutionId", institutionid)
                        .queryParam("productId", productId)
                        .queryParam("productRole", productRole)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .activateUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void deleteRelationship() throws Exception {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .delete(BASE_URL + "/{userId}", userId)
                        .queryParam("institutionId", institutionid)
                        .queryParam("productId", productId)
                        .queryParam("productRole", productRole)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(userServiceMock, Mockito.times(1))
                .deleteUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userServiceMock);
    }

    @Test
    void getUserById() throws Exception {
        //given
        final String userId = "userId";
        final String institutionId = "institutionId";
        final List<String> fields = List.of("fields");

        byte[] userStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "User.json"));
        User user = objectMapper.readValue(userStream, User.class);

        when(userServiceMock.getUserById(userId, institutionId, fields)).thenReturn(user);

        //when
        mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{id}", userId)
                        .param("institutionId", institutionId)
                        .param("fields", fields.get(0))
                        .contentType(APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserResource.json")))));

        //then
        verify(userServiceMock, times(1)).getUserById(userId, institutionId, fields);
    }

    @Test
    void search() throws Exception {
        //given
        final String externalId = "externalId";
        final String institutionId = "institutionId";
        SearchUserDto externalIdDto = new SearchUserDto();
        externalIdDto.setFiscalCode(externalId);

        byte[] userStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "User.json"));
        User user = objectMapper.readValue(userStream, User.class);

        Mockito.when(userServiceMock.searchUserByFiscalCode(externalId, institutionId))
                .thenReturn(user);
        //when
        mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/search")
                        .param("institutionId", institutionId)
                        .content(objectMapper.writeValueAsString(externalIdDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserResource.json")))));

        //then
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

        byte[] userInfoStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "UserInfo.json"));
        UserInfo userInfo = objectMapper.readValue(userInfoStream, UserInfo.class);
        List<UserInfo> userInfos = List.of(userInfo);

        when(userServiceMock.getUsersByInstitutionId(institutionId, productId, null, "userId")).thenReturn(userInfos);

        // when
        mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/institution/" + institutionId)
                        .principal(authentication)
                        .queryParam("productId", productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "ProductUserResource.json")))));

        // then
        verify(userServiceMock, times(1))
                .getUsersByInstitutionId(institutionId, productId, null, "userId");
        verifyNoMoreInteractions(userServiceMock);
    }


}
