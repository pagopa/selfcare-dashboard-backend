package it.pagopa.selfcare.dashboard.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.core.BrokerService;
import it.pagopa.selfcare.dashboard.core.ProductService;
import it.pagopa.selfcare.dashboard.web.model.mapper.BrokerResourceMapperImpl;
import it.pagopa.selfcare.dashboard.web.model.product.BrokerResource;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/v1/products";

    private static final String FILE_JSON_PATH = "src/test/resources/json/";

    @InjectMocks
    private ProductController productController;
    @Mock
    private ProductService productServiceMock;
    @Mock
    private BrokerService brokerServiceMock;
    @Spy
    private BrokerResourceMapperImpl brokerResourceMapper;

    @BeforeEach
    public void setUp() {
        super.setUp(productController);
    }

    @Test
    void getProductRoles() throws Exception {
        // given
        String productId = "prod1";
        byte[] productRoleInfoStream = Files.readAllBytes(Paths.get(FILE_JSON_PATH + "ProductRoleInfo.json"));
        ProductRoleInfo productRoleInfo = objectMapper.readValue(productRoleInfoStream, ProductRoleInfo.class);

        when(productServiceMock.getProductRoles(productId))
                .thenReturn(new EnumMap<>(PartyRole.class) {{
                    put(PartyRole.MANAGER, productRoleInfo);
                }});
        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{productId}/roles", productId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(new String(Files.readAllBytes(Paths.get(FILE_JSON_PATH + "ProductRoleMappingsResource.json")))))
                .andReturn();
        // then
        verify(productServiceMock, times(1))
                .getProductRoles(productId);
        verifyNoMoreInteractions(productServiceMock);
    }

    @Test
    void getProductBrokersProdPagoPA() throws Exception {
        // given
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setCode("code");
        brokerInfo.setDescription("description");

        String productId = "prod-pagopa";
        String institutionType = "PSP";
        when(brokerServiceMock.findAllByInstitutionType(institutionType))
                .thenReturn(List.of(
                        brokerInfo
                ));
        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{productId}/brokers/{institutionType}", productId, institutionType)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        List<BrokerResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(resources);
        assertEquals(1, resources.size());
        assertEquals(brokerInfo.getCode(), resources.get(0).getCode());
        assertEquals(brokerInfo.getDescription(), resources.get(0).getDescription());

        verify(brokerServiceMock, times(1))
                .findAllByInstitutionType(institutionType);
        verifyNoMoreInteractions(brokerServiceMock);
    }

    @Test
    void getProductBrokers() throws Exception {
        // given
        BrokerInfo brokerInfo = new BrokerInfo();
        brokerInfo.setCode("code");
        brokerInfo.setDescription("description");

        String productId = "prod-io";
        String institutionType = "PSP";
        when(brokerServiceMock.findInstitutionsByProductAndType(productId, institutionType))
                .thenReturn(List.of(
                        brokerInfo
                ));
        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URL + "/{productId}/brokers/{institutionType}", productId, institutionType)
                        .contentType(APPLICATION_JSON_VALUE)
                        .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        // then
        List<BrokerResource> resources = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertNotNull(resources);
        assertEquals(1, resources.size());
        assertEquals(brokerInfo.getCode(), resources.get(0).getCode());
        assertEquals(brokerInfo.getDescription(), resources.get(0).getDescription());

        verify(brokerServiceMock, times(1))
                .findInstitutionsByProductAndType(productId, institutionType);
        verifyNoMoreInteractions(brokerServiceMock);
    }

}
