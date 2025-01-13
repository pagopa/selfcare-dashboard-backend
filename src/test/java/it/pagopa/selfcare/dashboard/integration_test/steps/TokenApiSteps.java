package it.pagopa.selfcare.dashboard.integration_test.steps;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TokenApiSteps{

    @Autowired
    private DashboardStepsUtil dashboardStepsUtil;


    @And("the institution ID is {string} and the product ID is {string}")
    public void theInstitutionIDIsAndTheProductIDIs(String institutionId, String productId) {
        dashboardStepsUtil.filter.setInstitutionId(institutionId);
        dashboardStepsUtil.filter.setProductId(productId);
    }

    @And("the response should contain a valid token")
    public void theResponseShouldContainAValidToken() {
        String token = dashboardStepsUtil.responses.getIdentityTokenResource().getToken();
        DecodedJWT decodedJWT = JWT.decode(token);
        Map<String, Claim> payload = decodedJWT.getClaims();
        Assertions.assertEquals("Doe",payload.get("family_name").asString());
        Assertions.assertEquals("John",payload.get("name").asString());
        Assertions.assertEquals("PRVTNT80A41H401T",payload.get("fiscal_number").asString());
        Assertions.assertEquals("97a511a7-2acc-47b9-afed-2f3c65753b4a",payload.get("uid").asString());
        Assertions.assertEquals("c9a50656-f345-4c81-84be-5b2474470544",payload.get("organization").as(Map.class).get("id"));
        Assertions.assertEquals("Comune di Castelbuono",payload.get("organization").as(Map.class).get("name"));
        Assertions.assertEquals("00310810825",payload.get("organization").as(Map.class).get("fiscal_code"));
        Assertions.assertEquals("c_c067",payload.get("organization").as(Map.class).get("ipaCode"));
        List<Object> list = (List<Object>) payload.get("organization").as(Map.class).get("roles");
        Assertions.assertEquals(1,list.size());

    }

    @And("the institution ID is {string}")
    public void theInstitutionIDIs(String institutionId) {
        dashboardStepsUtil.filter.setInstitutionId(institutionId);
    }


    @When("I send a GET request to {string} with the given details to retrieve billing token")
    public void iSendAGETRequestToWithTheGivenDetailsToRetrieveBillingToken(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }
        if(Objects.nonNull(dashboardStepsUtil.filter.getLang())) {
            requestSpecification.queryParam("lang", dashboardStepsUtil.filter.getLang());
        }
        if(Objects.nonNull(dashboardStepsUtil.filter.getEnvironment())) {
            requestSpecification.queryParam("environment", dashboardStepsUtil.filter.getEnvironment());
        }
        if(Objects.nonNull(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil. status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setBackOfficeUrl(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil. errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} with the given details to retrieve token")
    public void iSendAGETRequestToWithTheGivenDetailsToRetrieveToken(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }
        if (Objects.nonNull(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }
        if (Objects.nonNull(dashboardStepsUtil.filter.getProductId())) {
            requestSpecification.queryParam("productId", dashboardStepsUtil.filter.getProductId());
        }
        if(Objects.nonNull(dashboardStepsUtil.filter.getEnvironment())) {
            requestSpecification.queryParam("environment", dashboardStepsUtil.filter.getEnvironment());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setIdentityTokenResource(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @And("the response should contain a {string} query param for language")
    public void theResponseShouldContainAValidLanguage(String lang) {
        String uri = dashboardStepsUtil.responses.getBackOfficeUrl().toString();
        Assertions.assertTrue(uri.contains("lang="+lang));
    }
}
