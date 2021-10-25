package it.pagopa.selfcare.dashboard.web.security;

import io.jsonwebtoken.Claims;
import it.pagopa.selfcare.dashboard.connector.rest.PartyRestClient;
import it.pagopa.selfcare.dashboard.web.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.Optional;


@WebMvcTest(value = {TestController.class})
@ContextConfiguration(classes = {
        TestController.class,
        SecurityConfig.class
})
class JwtAuthenticationFilterTest {

    private static final String BASE_URL = "/test";
    public static final UsernamePasswordAuthenticationToken USER_AUTHENTICATION =
            new UsernamePasswordAuthenticationToken("user", "", Collections.singletonList(new SimpleGrantedAuthority(Role.ROLE_USER.name())));


    @MockBean
    private JwtService jwtServiceMock;

    @MockBean
    private PartyRestClient partyRestClient;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    protected MockMvc mvc;


    @Test
    void testWithoutJwt() throws Exception {
        // given
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(Optional.empty());
        // when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNAUTHORIZED.value()))
                .andReturn();
    }


    @Test
    void testWithValidJwt() throws Exception {
        // given
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(Optional.of(Mockito.mock(Claims.class)));
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(USER_AUTHENTICATION);
        // when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
    }


    @Test
    void testWithInvalidJwt() throws Exception {
        // given
        Mockito.when(jwtServiceMock.getClaims(Mockito.any()))
                .thenReturn(Optional.empty());
        // when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNAUTHORIZED.value()))
                .andReturn();
    }


    @Test
    void testWithException() throws Exception {
        // given
        Mockito.doThrow(RuntimeException.class)
                .when(jwtServiceMock).getClaims(Mockito.any());
        // when
        mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNAUTHORIZED.value()))
                .andReturn();
    }

}