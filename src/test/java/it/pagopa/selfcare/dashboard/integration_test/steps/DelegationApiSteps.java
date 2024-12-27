package it.pagopa.selfcare.dashboard.integration_test.steps;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.specification.RequestSpecification;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationRequestDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DelegationApiSteps{

    @Autowired
    private DashboardStepsUtil dashboardStepsUtil;

    @DataTableType
    public DelegationRequestDto convertRequest(Map<String, String> entry) {
        return dashboardStepsUtil.toDelegationRequestDto(entry);
    }


    @And("the following delegation request details:")
    public void theFollowingSupportRequestDetails(List<DelegationRequestDto> delegationRequestDtos) {
        if (delegationRequestDtos != null && delegationRequestDtos.size() == 1)
            dashboardStepsUtil.requests.setDelegationRequestDto(delegationRequestDtos.get(0));
    }

    @When("I send a POST request to {string} to create a delegation")
    public void iSendAPOSTRequestToToCreateADelegation(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .body(dashboardStepsUtil.requests.getDelegationRequestDto())
                .post(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 201) {
            dashboardStepsUtil.responses.setDelegationIdResource(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve paginated delegations")
    public void whenISendAGetRequestToRetrievePaginatedDelegations(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }
        if(Objects.nonNull(dashboardStepsUtil.filter.getSize())) {
            requestSpecification.queryParam("size", dashboardStepsUtil.filter.getSize());
        }
        if(Objects.nonNull(dashboardStepsUtil.filter.getPage())) {
            requestSpecification.queryParam("page", dashboardStepsUtil.filter.getPage());
        }
        if (Objects.nonNull(dashboardStepsUtil.filter.getProductId())) {
            requestSpecification.queryParam("productId", dashboardStepsUtil.filter.getProductId());
        }
        if (Objects.nonNull(dashboardStepsUtil.filter.getSearch())) {
            requestSpecification.queryParam("search", dashboardStepsUtil.filter.getSearch());
        }
        if (Objects.nonNull(dashboardStepsUtil.filter.getOrder())) {
            requestSpecification.queryParam("order", dashboardStepsUtil.filter.getOrder());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setDelegationWithPagination(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve delegations")
    public void whenISendAGetRequestToRetrieveDelegations(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }
        if (Objects.nonNull(dashboardStepsUtil.filter.getProductId())) {
            requestSpecification.queryParam("productId", dashboardStepsUtil.filter.getProductId());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setDelegationResource(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @And("the response should contain a list of delegations")
    public void theResponseShouldContainAListOfDelegations() {

    }

    @And("the response should contain an empty list")
    public void theResponseShouldContainAnEmptyList() {
        Assertions.assertTrue(dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().isEmpty());
    }

    @And("the response should contain a list of delegations filtered by {string}")
    public void theResponseShouldContainAListOfDelegationsFilteredBy(String search) {

    }

    @And("the response should be ordered by {string}")
    public void theResponseShouldBeOrderedBy(String order) {

    }

    @And("the response contains delegation only for {string}")
    public void theResponseContainsDelegationOnlyFor(String productId) {
        Assertions.assertTrue(dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().stream()
                .allMatch(delegation -> delegation.getProductId().equals(productId)));
    }

    @And("the response should contain a list of paginated delegations")
    public void theResponseShouldContainAListOfPaginatedDelegations() {

    }

    @And("the response should contain a list of paginated delegations filtered by {string}")
    public void theResponseShouldContainAListOfPaginatedDelegationsFilteredBy(String arg0) {

    }

    @And("the paginated response contains delegation only for {string}")
    public void thePaginatedResponseContainsDelegationOnlyFor(String arg0) {
    }

    @And("the response should contain a delegation id")
    public void theResponseShouldContainADelegationId() {
        Assertions.assertTrue(StringUtils.isNotBlank(dashboardStepsUtil.responses.getDelegationIdResource().getId()));
    }
}
