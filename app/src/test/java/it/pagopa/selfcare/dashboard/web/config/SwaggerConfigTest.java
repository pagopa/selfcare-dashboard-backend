package it.pagopa.selfcare.dashboard.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.dashboard.core.*;
import it.pagopa.selfcare.dashboard.web.model.mapper.BrokerMapperImpl;
import it.pagopa.selfcare.dashboard.web.security.ExchangeTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        SwaggerConfig.class,
        WebConfig.class,
        BrokerMapperImpl.class
})
@EnableOpenApi
@EnableWebMvc
@ComponentScan(basePackages = "it.pagopa.selfcare.dashboard.web.controller")
@TestPropertySource(locations = "classpath:config/application.yml")
class SwaggerConfigTest {

    @MockBean
    private FileStorageService storageServiceMock;

    @MockBean
    private InstitutionService institutionServiceMock;

    @MockBean
    private PnPGInstitutionService pnPGInstitutionService;

    @MockBean
    private ExchangeTokenService exchangeTokenServiceMock;

    @MockBean
    private ProductService productServiceMock;

    @MockBean
    private RelationshipService relationshipServiceMock;

    @MockBean
    private UserService userServiceMock;

    @MockBean
    private BrokerService brokerService;

    @MockBean
    private UserGroupService userGroupServiceMock;

    @Autowired
    WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpringPlugin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo((result) -> {
                    assertNotNull(result);
                    assertNotNull(result.getResponse());
                    final String content = result.getResponse().getContentAsString();
                    assertFalse(content.isBlank());
                    Object swagger = objectMapper.readValue(result.getResponse().getContentAsString(), Object.class);
                    String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(swagger);
                    Path basePath = Paths.get("src/main/resources/swagger/");
                    Files.createDirectories(basePath);
                    Files.write(basePath.resolve("api-docs.json"), formatted.getBytes());
                    assertFalse(content.contains("${"), "Generated swagger contains placeholders");
                });
    }

}