package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.web.config.WebTestConfig;
import it.pagopa.selfcare.dashboard.web.handler.DashboardExceptionsHandler;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MimeTypeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(value = {InstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionController.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class InstitutionControllerTest {

    private static final String BASE_URL = "/institutions";
    private static final ProductTree PRODUCT = TestUtils.mockInstance(new ProductTree());

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private FileStorageService storageServiceMock;

    @MockBean
    private InstitutionService institutionServiceMock;


    @Test
    void saveInstitutionLogo() throws Exception {
        // given
        String institutionId = "institutionId";
        String contentType = MimeTypeUtils.IMAGE_JPEG_VALUE;
        String filename = "test.jpeg";
        MockMultipartFile multipartFile = new MockMultipartFile("logo", filename,
                contentType, "test institution logo".getBytes());
        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart(BASE_URL + "/" + institutionId + "/logo")
                .file(multipartFile);
        requestBuilder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });
        // when
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
        // then
        Mockito.verify(storageServiceMock, Mockito.times(1))
                .storeInstitutionLogo(Mockito.eq(institutionId), Mockito.any(), Mockito.eq(contentType), Mockito.eq(filename));
        Mockito.verifyNoMoreInteractions(storageServiceMock);
    }

    @Test
    void getInstitution_institutionInfoNotNull() throws Exception {
        // given
        String institutionId = "institutionId";
        Mockito.when(institutionServiceMock.getInstitution(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> {
                    String id = invocationOnMock.getArgument(0, String.class);
                    InstitutionInfo institutionInfo = new InstitutionInfo();
                    institutionInfo.setInstitutionId(id);
                    return institutionInfo;
                });
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        InstitutionResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionResource.class);
        assertNotNull(resource);
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitution_institutionInfoNull() throws Exception {
        // given
        String institutionId = "institutionId";
        Mockito.when(institutionServiceMock.getInstitution(Mockito.anyString()))
                .thenReturn(null);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        assertEquals("", result.getResponse().getContentAsString());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutions_institutionInfoNotNull() throws Exception {
        // given
        Mockito.when(institutionServiceMock.getInstitutions())
                .thenAnswer(invocationOnMock -> List.of(TestUtils.mockInstance(new InstitutionInfo())));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(resources);
        assertFalse(resources.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutions();
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionProducts_notNull() throws Exception {
        // given
        String institutionId = "institutionId";
        Mockito.when(institutionServiceMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.singletonList(PRODUCT));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/products", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionProducts_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        Mockito.when(institutionServiceMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/products", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionUsers_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        Mockito.when(institutionServiceMock.getInstitutionUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/users", institutionId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionUsers(institutionId, Optional.empty(), Optional.empty(), Optional.empty());
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionUsers_notEmpty() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "institutionId";
        SelfCareAuthority role = SelfCareAuthority.ADMIN;
        String[] productRole = {"api", "security"};
        Mockito.when(institutionServiceMock.getInstitutionUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(TestUtils.mockInstance(new UserInfo())));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/users", institutionId)
                .queryParam("role", role.toString())
                .queryParam("productId", productId)
                .queryParam("productRoles", productRole)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionUsers(institutionId, Optional.of(productId), Optional.of(role), Optional.of(Set.of(productRole)));
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionUser_nullUser() throws Exception {
        //given
        String institutionId = "institutionId";
        String userId = "notFound";
        Mockito.when(institutionServiceMock.getInstitutionUser(Mockito.any(), Mockito.any()))
                .thenThrow(ResourceNotFoundException.class);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        //then
        Assertions.assertEquals(0, result.getResponse().getContentLength());

    }

    @Test
    void getInstitutionUser_notNullUser() throws Exception {
        //given
        String institutionId = "institutionId";
        String userId = "notFound";
        UserInfo userInfo = TestUtils.mockInstance(new UserInfo());

        Mockito.when(institutionServiceMock.getInstitutionUser(Mockito.any(), Mockito.any()))
                .thenReturn(userInfo);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        //then
        InstitutionUserResource userResource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionUserResource.class);
        assertNotNull(userResource);
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionUser(institutionId, userId);
        Mockito.verifyNoMoreInteractions(institutionServiceMock);

    }

    @Test
    void getInstitutionProductUsers_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Mockito.when(institutionServiceMock.getInstitutionProductUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionProductUsers(institutionId, productId, Optional.empty(), Optional.empty());
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionProductUsers_notEmpty() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        SelfCareAuthority role = SelfCareAuthority.ADMIN;
        String[] productRole = {"api", "security"};
        Mockito.when(institutionServiceMock.getInstitutionProductUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(TestUtils.mockInstance(new UserInfo())));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                .queryParam("role", role.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .getInstitutionProductUsers(institutionId, productId, Optional.of(role), Optional.empty());
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void createInstitutionProductUser() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        CreateUserDto user = TestUtils.mockInstance(new CreateUserDto(), "setProductRoles");
        Set<String> productRoles = Set.of("productRole");
        user.setProductRoles(productRoles);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        // then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(institutionServiceMock, Mockito.times(1))
                .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), Mockito.notNull());
        Mockito.verifyNoMoreInteractions(institutionServiceMock);
    }

}