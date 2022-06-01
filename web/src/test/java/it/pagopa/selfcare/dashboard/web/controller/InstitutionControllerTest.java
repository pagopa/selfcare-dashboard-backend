package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
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
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MimeTypeUtils;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {InstitutionController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {InstitutionController.class, WebTestConfig.class, DashboardExceptionsHandler.class})
class InstitutionControllerTest {

    private static final String BASE_URL = "/institutions";
    private static final ProductTree PRODUCT;

    static {
        PRODUCT = mockInstance(new ProductTree());
        PRODUCT.setChildren(List.of(mockInstance(new Product())));
    }

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
                .andExpect(status().isOk());
        // then
        verify(storageServiceMock, times(1))
                .storeInstitutionLogo(Mockito.eq(institutionId), any(), Mockito.eq(contentType), Mockito.eq(filename));
        verifyNoMoreInteractions(storageServiceMock);
    }

    @Test
    void getInstitution_institutionInfoNotNull() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getInstitution(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> {
                    String id = invocationOnMock.getArgument(0, String.class);
                    InstitutionInfo institutionInfo = new InstitutionInfo();
                    institutionInfo.setExternalId(id);
                    return institutionInfo;
                });
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        InstitutionResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionResource.class);
        assertNotNull(resource);
        verify(institutionServiceMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitution_institutionInfoNull() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getInstitution(Mockito.anyString()))
                .thenReturn(null);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        assertEquals("", result.getResponse().getContentAsString());
        verify(institutionServiceMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutions_institutionInfoNotNull() throws Exception {
        // given
        when(institutionServiceMock.getInstitutions())
                .thenAnswer(invocationOnMock -> List.of(mockInstance(new InstitutionInfo())));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/")
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
        verify(institutionServiceMock, times(1))
                .getInstitutions();
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionProducts_notNull() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getInstitutionProducts(any()))
                .thenReturn(singletonList(PRODUCT));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/products", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(1, products.get(0).getChildren().size());
        verify(institutionServiceMock, times(1))
                .getInstitutionProducts(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionProducts_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getInstitutionProducts(any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/products", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<ProductsResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(institutionServiceMock, times(1))
                .getInstitutionProducts(institutionId);
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionUsers_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        when(institutionServiceMock.getInstitutionUsers(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users", institutionId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(institutionServiceMock, times(1))
                .getInstitutionUsers(institutionId, Optional.empty(), Optional.empty(), Optional.empty());
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionUsers_notEmpty() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "institutionId";
        SelfCareAuthority role = SelfCareAuthority.ADMIN;
        String[] productRole = {"api", "security"};
        final UserInfo userInfo = mockInstance(new UserInfo(), "setId");
        userInfo.setId(randomUUID().toString());
        when(institutionServiceMock.getInstitutionUsers(any(), any(), any(), any()))
                .thenReturn(singletonList(userInfo));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users", institutionId)
                        .queryParam("role", role.toString())
                        .queryParam("productId", productId)
                        .queryParam("productRoles", productRole)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        verify(institutionServiceMock, times(1))
                .getInstitutionUsers(institutionId, Optional.of(productId), Optional.of(role), Optional.of(Set.of(productRole)));
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionUser_nullUser() throws Exception {
        //given
        String institutionId = "institutionId";
        String userId = "notFound";
        when(institutionServiceMock.getInstitutionUser(any(), any()))
                .thenThrow(ResourceNotFoundException.class);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        //then
        Assertions.assertEquals(0, result.getResponse().getContentLength());

    }

    @Test
    void getInstitutionUser_notNullUser() throws Exception {
        //given
        String institutionId = "institutionId";
        String userId = "notFound";
        UserInfo userInfo = mockInstance(new UserInfo(), "setId");
        userInfo.setId(randomUUID().toString());

        when(institutionServiceMock.getInstitutionUser(any(), any()))
                .thenReturn(userInfo);
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/users/{userId}", institutionId, userId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        InstitutionUserResource userResource = objectMapper.readValue(result.getResponse().getContentAsString(), InstitutionUserResource.class);
        assertNotNull(userResource);
        verify(institutionServiceMock, times(1))
                .getInstitutionUser(institutionId, userId);
        verifyNoMoreInteractions(institutionServiceMock);

    }

    @Test
    void getInstitutionProductUsers_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        when(institutionServiceMock.getInstitutionProductUsers(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(institutionServiceMock, times(1))
                .getInstitutionProductUsers(institutionId, productId, Optional.empty(), Optional.empty());
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void getInstitutionProductUsers_notEmpty() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        SelfCareAuthority role = SelfCareAuthority.ADMIN;
        final UserInfo userInfoModel = mockInstance(new UserInfo());
        userInfoModel.setId(randomUUID().toString());
        when(institutionServiceMock.getInstitutionProductUsers(any(), any(), any(), any()))
                .thenReturn(singletonList(userInfoModel));
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .queryParam("role", role.toString())
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        List<InstitutionUserResource> products = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(products);
        assertFalse(products.isEmpty());
        verify(institutionServiceMock, times(1))
                .getInstitutionProductUsers(institutionId, productId, Optional.of(role), Optional.empty());
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void createInstitutionProductUser() throws Exception {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        CreateUserDto user = mockInstance(new CreateUserDto(), "setProductRoles");
        Set<String> productRoles = Set.of("productRole");
        user.setProductRoles(productRoles);
        UserId userId = mockInstance(new UserId());
        when(institutionServiceMock.createUsers(any(), any(), any()))
                .thenReturn(userId);
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .post(BASE_URL + "/{institutionId}/products/{productId}/users", institutionId, productId)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();
        // then
        UserIdResource resource = objectMapper.readValue(result.getResponse().getContentAsString(), UserIdResource.class);
        assertNotNull(resource);
        verify(institutionServiceMock, times(1))
                .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), Mockito.notNull());
        verifyNoMoreInteractions(institutionServiceMock);
    }

    @Test
    void addProductUserRole() throws Exception {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = UUID.randomUUID().toString();
        UserProductRoles productRoles = new UserProductRoles();
        productRoles.setProductRoles(Set.of("productRole"));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                        .put(BASE_URL + "/{institutionId}/products/{productId}/users/{userId}", institutionId, productId, userId)
                        .content(objectMapper.writeValueAsString(productRoles))
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        ArgumentCaptor<it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto> userCaptor = ArgumentCaptor.forClass(it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.class);
        verify(institutionServiceMock, times(1))
                .addUserProductRoles(eq(institutionId), eq(productId), eq(userId), userCaptor.capture());
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto capturedUser = userCaptor.getValue();
        assertNull(capturedUser.getUser());
        assertEquals("", capturedUser.getEmail());
        assertEquals("", capturedUser.getName());
        assertEquals("", capturedUser.getSurname());
        assertEquals("", capturedUser.getTaxCode());
        capturedUser.getRoles().forEach(role -> {
            assertTrue(productRoles.getProductRoles().contains(role.getProductRole()));
        });
    }

}