package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportResponse;
import it.pagopa.selfcare.dashboard.core.SupportService;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.SupportMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.SupportMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.support.SupportRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {DelegationController.class})
@ExtendWith(MockitoExtension.class)
class SupportControllerTest {

    @InjectMocks
    private SupportController supportController;

    @Mock
    private SupportService supportService;

    @Spy
    private SupportMapper supportMapper = new SupportMapperImpl();

    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Method under test: {@link SupportController#sendSupportRequest(SupportRequestDto, Authentication)}
     */
    @Test
    void testSendSupportRequest() throws Exception {

        String redirectUrl = "test";
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        SelfCareUser user = SelfCareUser.builder("id")
                .surname("surname")
                .name("name")
                .fiscalCode("FFFFFFFF")
                .build();
        when(authentication.getPrincipal()).thenReturn(user);
        when(supportService.sendRequest(any())).thenReturn(redirectUrl);

        SupportRequestDto supportRequest = new SupportRequestDto();
        supportRequest.setEmail("test@gmail.com");
        String content = (new ObjectMapper()).writeValueAsString(supportRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/support")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MvcResult result =  MockMvcBuilders.standaloneSetup(supportController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andReturn();

        SupportResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(response);
        assertNotNull(response.getRedirectUrl());
        assertEquals(response.getRedirectUrl(), redirectUrl);
    }

    /**
     * Method under test: {@link DelegationController#createDelegation(DelegationRequestDto)}
     */
    @Test
    void testSendBadRequest() throws Exception {
        SupportRequestDto supportRequest = new SupportRequestDto();
        supportRequest.setEmail("pp");
        String content = (new ObjectMapper()).writeValueAsString(supportRequest);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        MockMvcBuilders.standaloneSetup(supportController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }
}
