package it.pagopa.selfcare.dashboard.web.controller;

import it.pagopa.selfcare.dashboard.core.RelationshipService;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(value = {RelationshipController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {RelationshipController.class, WebTestConfig.class})
class RelationshipControllerTest {

    private static final String BASE_URL = "/relationships";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private RelationshipService relationshipServiceMock;


    @Test
    void suspendRelationship() throws Exception {
        // given
        String relationshipId = "rel1";
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{relationshipId}/suspend", relationshipId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(relationshipServiceMock, Mockito.times(1))
                .suspend(relationshipId);
        Mockito.verifyNoMoreInteractions(relationshipServiceMock);
    }


    @Test
    void activateRelationship() throws Exception {
        // given
        String relationshipId = "rel1";
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{relationshipId}/activate", relationshipId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(relationshipServiceMock, Mockito.times(1))
                .activate(relationshipId);
        Mockito.verifyNoMoreInteractions(relationshipServiceMock);
    }

    @Test
    void deleteRelationship() throws Exception {
        // given
        String relationshipId = "rel1";
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/{relationshipId}", relationshipId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(relationshipServiceMock, Mockito.times(1))
                .delete(relationshipId);
        Mockito.verifyNoMoreInteractions(relationshipServiceMock);
    }

}