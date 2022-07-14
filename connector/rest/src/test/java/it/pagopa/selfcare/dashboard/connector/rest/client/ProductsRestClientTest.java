package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.rest.config.ProductsRestClientTestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(
        locations = "classpath:config/products-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest",
                "feign.okhttp.enabled=true"
        })
@ContextConfiguration(
        initializers = ProductsRestClientTest.RandomPortInitializer.class,
        classes = {ProductsRestClientTestConfig.class, HttpClientConfiguration.class})
class ProductsRestClientTest extends BaseFeignRestClientTest {

    @Order(1)
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(RestTestUtils.getWireMockConfiguration("stubs/products"))
            .build();

    @Autowired
    private ProductsRestClient restClient;


    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_PRODUCT_URL=%s",
                            wm.getRuntimeInfo().getHttpBaseUrl())
            );
        }
    }


    @Test
    void getProducts() {
        // given and when
        List<Product> response = restClient.getProducts();
        // then
        assertFalse(response.isEmpty());
    }


    @Test
    void getProductRoleMappings() {
        // given
        String productId = "productId";
        // when
        Map<PartyRole, ProductRoleInfo> response = restClient.getProductRoleMappings(productId);
        // then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void getProductsTree() {
        //given
        //when
        List<ProductTree> response = restClient.getProductsTree();
        //then
        assertFalse(response.isEmpty());
        assertNotNull(response.get(0).getNode().getLogoBgColor());
        assertEquals(1, response.get(0).getChildren().size());
    }

    @Test
    void getProduct_fullyValued() {
        //given
        String productId = "productId";
        //when
        Product product = restClient.getProduct(productId);
        //then
        assertNotNull(product);
        assertNotNull(product.getIdentityTokenAudience());
    }

}