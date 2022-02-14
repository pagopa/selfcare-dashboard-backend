package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.core.UserRegistryService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.model.EmbeddedExternalIdDto;
import it.pagopa.selfcare.dashboard.web.model.UserResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@WebMvcTest(value = {UserController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {UserController.class, WebTestConfig.class})
class UserControllerTest {

    private static final String BASE_URL = "/users";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    private UserRegistryService userRegistryServiceMock;

    @Test
    void getUser_notNull() throws Exception {
        //given
        String externalId = "externalId";
        EmbeddedExternalIdDto externalIdDto = new EmbeddedExternalIdDto();
        externalIdDto.setExternalId(externalId);
        User user = TestUtils.mockInstance(new User());
        Mockito.when(userRegistryServiceMock.getUser(Mockito.anyString()))
                .thenReturn(user);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/external-id")
                .content(mapper.writeValueAsString(externalIdDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //then
        UserResource userResponse = mapper.readValue(result.getResponse().getContentAsString(), UserResource.class);
        Assertions.assertNotNull(userResponse);
        Mockito.verify(userRegistryServiceMock, Mockito.times(1))
                .getUser(externalId);
        Mockito.verifyNoMoreInteractions(userRegistryServiceMock);
    }




}