package it.pagopa.selfcare.dashboard.connector.rest.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import it.pagopa.selfcare.commons.connector.rest.BaseFeignRestClientTest;
import it.pagopa.selfcare.commons.connector.rest.RestTestUtils;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.rest.config.ProductsRestClientTestConfig;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.List;
import java.util.Map;

@TestPropertySource(
        locations = "classpath:config/products-rest-client.properties",
        properties = {
                "logging.level.it.pagopa.selfcare.dashboard.connector.rest=DEBUG",
                "spring.application.name=selc-dashboard-connector-rest"
        })
@ContextConfiguration(
        initializers = ProductsRestClientTest.RandomPortInitializer.class,
        classes = {ProductsRestClientTestConfig.class})
public class ProductsRestClientTest extends BaseFeignRestClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule;

    @Autowired
    private ProductsRestClient restClient;

    static {
        WireMockConfiguration config = RestTestUtils.getWireMockConfiguration("stubs");
        wireMockRule = new WireMockClassRule(config);
    }

    public static class RandomPortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    String.format("MS_PRODUCT_URL=http://%s:%d",
                            wireMockRule.getOptions().bindAddress(),
                            wireMockRule.port())
            );
        }
    }


    @Test
    public void getProducts() {
        // given and when
        List<Product> response = restClient.getProducts();
        // then
        Assert.assertFalse(response.isEmpty());
    }


    @Test
    public void getProductRoleMappings() {
        // given
        String productId = "productId";
        // when
        Map<String, List<String>> response = restClient.getProductRoleMappings(productId);
        System.out.println("response = " + response);
        // then
        Assert.assertNotNull(response);
        Assert.assertFalse(response.isEmpty());
    }

}