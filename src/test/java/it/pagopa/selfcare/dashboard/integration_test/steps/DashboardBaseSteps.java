package it.pagopa.selfcare.dashboard.integration_test.steps;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import it.pagopa.selfcare.dashboard.integration_test.model.Filter;
import it.pagopa.selfcare.dashboard.integration_test.model.JwtData;
import it.pagopa.selfcare.dashboard.integration_test.model.TestProperties;
import it.pagopa.selfcare.dashboard.integration_test.utils.KeyGenerator;
import it.pagopa.selfcare.dashboard.model.delegation.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class DashboardBaseSteps{

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private  DashboardStepsUtil dashboardStepsUtil;

    @Given("user login with username {string} and password {string}")
    public void login(String user, String pass) {
        dashboardStepsUtil.filter = new Filter();
        TestProperties testProperties = readDataPopulation();
        JwtData jwtData = testProperties.getJwtData().stream()
                .filter(data -> data.getUsername().equals(user) && data.getPassword().equals(pass))
                .findFirst()
                .orElse(null);
        dashboardStepsUtil.token = generateToken(jwtData);
    }

    public TestProperties readDataPopulation() {
        TestProperties testProperties = null;
        try {
            testProperties = objectMapper.readValue(new File("src/test/resources/dataPopulation/data.json"), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testProperties;
    }

    public String generateToken(JwtData jwtData) {
        if (Objects.nonNull(jwtData)) {
            try {
                File file = new File("integration-test-config/key/private-key.pem");
                Algorithm alg = Algorithm.RSA256(KeyGenerator.getPrivateKey(new String(Files.readAllBytes(file.toPath()))));
                String jwt = JWT.create()
                        .withHeader(jwtData.getJwtHeader())
                        .withPayload(jwtData.getJwtPayload())
                        .withIssuedAt(Instant.now())
                        .withExpiresAt(Instant.now().plusSeconds(3600))
                        .sign(alg);
                log.info("generated token jwt");
                return jwt;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @And("the productId is {string}")
    public void theProductIdIs(String productId) {
        dashboardStepsUtil.filter.setProductId(productId);
    }

    @And("the environment is {string}")
    public void theEnvironmentIs(String env) {
        dashboardStepsUtil.filter.setEnvironment(env);
    }

    @And("the institutionType is {string}")
    public void theInstitutionTypeIs(String institutionType) {
        dashboardStepsUtil.filter.setInstitutionType(institutionType);
    }

    @And("the language is {string}")
    public void languageIs(String lang) {
        dashboardStepsUtil.filter.setLang(lang);
    }


    @And("the institutionId is {string}")
    public void theInstitutionIdIs(String institutionId) {
        dashboardStepsUtil.filter.setInstitutionId(institutionId);
    }

    @And("I have groupId {string}")
    public void iHaveGroupId(String groupId) {
        dashboardStepsUtil.filter.setGroupId(groupId);
    }

    @And("I have memberId {string}")
    public void iHaveMemberID(String memberId) {
        dashboardStepsUtil.filter.setUserId(memberId);
    }

    @And("require page {int} and size {int} and order by {string}")
    public void requirePageAndSizeAndOrderBy(int page, int size, String order) {
        dashboardStepsUtil.filter.setPage(page);
        dashboardStepsUtil.filter.setSize(size);
        dashboardStepsUtil.filter.setOrder(Order.valueOf(order));

    }

    @And("I have {string} as search filter")
    public void iHaveAsSearchFilter(String search) {
        dashboardStepsUtil.filter.setSearch(search);
    }

    @And("I have {string} as order")
    public void iHaveAsOrder(String order) {
        dashboardStepsUtil.filter.setOrder(Order.valueOf(order));
    }

    @And("I have {string} as size filter")
    public void iHaveAsSizeFilter(int size) {
        dashboardStepsUtil.filter.setSize(size);
    }


    @And("I set the page number to {int} and page size to {int}")
    public void iSetThePageNumberToAndPageSizeTo(int page, int size) {
        dashboardStepsUtil.filter.setPage(page);
        dashboardStepsUtil.filter.setSize(size);
    }

    @Given("I have a filter with sorting by {string}")
    public void iHaveAFilterWithSortingBy(String sort) {
        dashboardStepsUtil.filter.setSort(sort);
    }


    @And("the response should contain an error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {
        String[] errorMessageArray = expectedErrorMessage.split(",");
        Arrays.stream(errorMessageArray).forEach(s -> Assertions.assertTrue(dashboardStepsUtil.errorMessage.contains(s)));
    }

    @Then("the response status should be {int}")
    public void verifyResponseStatus(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, dashboardStepsUtil.status);
    }

}
