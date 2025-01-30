package it.pagopa.selfcare.dashboard.integration_test.steps;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.specification.RequestSpecification;
import it.pagopa.selfcare.dashboard.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.model.GeographicTaxonomyDto;
import it.pagopa.selfcare.dashboard.model.GeographicTaxonomyListDto;
import it.pagopa.selfcare.dashboard.model.UpdateInstitutionDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.pagopa.selfcare.dashboard.model.delegation.DelegationType.PT;

public class InstitutionApiSteps {

    @Autowired
    private DashboardStepsUtil dashboardStepsUtil;


    @DataTableType
    public GeographicTaxonomyDto convertGeoTaxonomyRequest(Map<String, String> entry) {
        return dashboardStepsUtil.toGeographicTaxonomyDto(entry);
    }

    @DataTableType
    public UpdateInstitutionDto convertUpdateInstitutionRequest(Map<String, String> entry) {
        return dashboardStepsUtil.toUpdateInstitutionDto(entry);
    }

    @DataTableType
    public CreateUserDto convertCreateUserRequest(Map<String, String> entry) {
        return dashboardStepsUtil.toCreateUserDto(entry);
    }

    @When("I send a POST request to {string} to create a institution")
    public void iSendAPOSTRequestToToCreateAnInstitution(String url) {
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

    @When("I send a GET request to {string} to retrieve paginated institutions")
    public void whenISendAGetRequestToRetrievePaginatedInstitutions(String url) {
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

    @When("I send a GET request to {string} to retrieve institutions logo")
    public void whenISendAGetRequestToRetrieveInstitutionsLogo(String url) {
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

    @When("I send a GET request to {string} to retrieve institutions")
    public void whenISendAGetRequestToRetrieveInstitutions(String url) {
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
            dashboardStepsUtil.responses.setInstitutionResource(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @And("the following geo-taxonomy request details:")
    public void theFollowingGeoTaxonomyRequestDetails(List<GeographicTaxonomyDto> geographicTaxonomyDtos) {
        GeographicTaxonomyListDto geographicTaxonomyListDto = new GeographicTaxonomyListDto();
        if (geographicTaxonomyDtos != null && geographicTaxonomyDtos.size() == 1) {
            geographicTaxonomyListDto.setGeographicTaxonomyDtoList(List.of(geographicTaxonomyDtos.get(0)));
        }
        dashboardStepsUtil.requests.setGeographicTaxonomyListDto(geographicTaxonomyListDto);
    }


    @When("I send a PUT request to {string} to update institutions geo-taxonomy")
    public void whenISendAPutRequestToUpdateGeoTaxonomy(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .body((dashboardStepsUtil.requests.getGeographicTaxonomyListDto()))
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .put(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
    }

    @And("the following institution description request details:")
    public void theFollowingInstitutionDescriptionRequestDetails(List<UpdateInstitutionDto> updateInstitutionDtos) {
        if (updateInstitutionDtos != null && updateInstitutionDtos.size() == 1) {
            dashboardStepsUtil.requests.setUpdateInstitutionDto(updateInstitutionDtos.get(0));
        }
    }

    @When("I send a PUT request to {string} to update institution description")
    public void whenISendAPutRequestToUpdateDescription(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .body((dashboardStepsUtil.requests.getUpdateInstitutionDto()))
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .put(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setInstitution(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }


    @And("the response should contain a list of institutions for all product")
    public void theResponseShouldContainAListOfInstitutionsForALlProduct() {
        Assertions.assertFalse(dashboardStepsUtil.responses.getDelegationResource().isEmpty());
        Assertions.assertEquals(2, dashboardStepsUtil.responses.getDelegationResource().size());
        Assertions.assertEquals("prod-io", dashboardStepsUtil.responses.getDelegationResource().get(0).getProductId());
        Assertions.assertEquals("prod-pagopa", dashboardStepsUtil.responses.getDelegationResource().get(1).getProductId());
        Assertions.assertEquals("c9a50656-f345-4c81-84be-5b2474470544", dashboardStepsUtil.responses.getDelegationResource().get(0).getInstitutionId());
        Assertions.assertEquals("c9a50656-f345-4c81-84be-5b2474470544", dashboardStepsUtil.responses.getDelegationResource().get(1).getInstitutionId());
        Assertions.assertEquals("Comune di Castelbuono", dashboardStepsUtil.responses.getDelegationResource().get(0).getInstitutionName());
        Assertions.assertEquals("Comune di Castelbuono", dashboardStepsUtil.responses.getDelegationResource().get(1).getInstitutionName());
        Assertions.assertEquals("PT test", dashboardStepsUtil.responses.getDelegationResource().get(0).getBrokerName());
        Assertions.assertEquals("PT test", dashboardStepsUtil.responses.getDelegationResource().get(1).getBrokerName());
        Assertions.assertEquals("067327d3-bdd6-408d-8655-87e8f1960046", dashboardStepsUtil.responses.getDelegationResource().get(0).getBrokerId());
        Assertions.assertEquals("067327d3-bdd6-408d-8655-87e8f1960046", dashboardStepsUtil.responses.getDelegationResource().get(1).getBrokerId());
        Assertions.assertEquals(PT, dashboardStepsUtil.responses.getDelegationResource().get(0).getType());
        Assertions.assertEquals(PT, dashboardStepsUtil.responses.getDelegationResource().get(1).getType());

    }

    @And("the response should contain a list of institutions only for {string}")
    public void theResponseShouldContainAListOfInstitutions(String productId) {
        Assertions.assertFalse(dashboardStepsUtil.responses.getDelegationResource().isEmpty());
        Assertions.assertEquals(1, dashboardStepsUtil.responses.getDelegationResource().size());
        Assertions.assertEquals(productId, dashboardStepsUtil.responses.getDelegationResource().get(0).getProductId());
    }



    @And("the response should contain a list of paginated institutions")
    public void theResponseShouldContainAListOfPaginatedInstitutions() {
        Assertions.assertFalse(dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().isEmpty());
        Assertions.assertEquals(2, dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().size());
        Assertions.assertEquals(2, dashboardStepsUtil.responses.getDelegationWithPagination().getPageInfo().getTotalElements());
        Assertions.assertEquals(1, dashboardStepsUtil.responses.getDelegationWithPagination().getPageInfo().getTotalPages());
        Assertions.assertEquals(0, dashboardStepsUtil.responses.getDelegationWithPagination().getPageInfo().getPageNo());
        Assertions.assertEquals("prod-io", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(0).getProductId());
        Assertions.assertEquals("prod-pagopa", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(1).getProductId());
        Assertions.assertEquals("c9a50656-f345-4c81-84be-5b2474470544", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(0).getInstitutionId());
        Assertions.assertEquals("c9a50656-f345-4c81-84be-5b2474470544", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(1).getInstitutionId());
        Assertions.assertEquals("Comune di Castelbuono", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(0).getInstitutionName());
        Assertions.assertEquals("Comune di Castelbuono", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(1).getInstitutionName());
        Assertions.assertEquals("PT test", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(0).getBrokerName());
        Assertions.assertEquals("PT test", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(1).getBrokerName());
        Assertions.assertEquals("067327d3-bdd6-408d-8655-87e8f1960046", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(0).getBrokerId());
        Assertions.assertEquals("067327d3-bdd6-408d-8655-87e8f1960046", dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(1).getBrokerId());
        Assertions.assertEquals(PT, dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(0).getType());
        Assertions.assertEquals(PT, dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().get(1).getType());
    }

    @And("the response should contain a list of paginated institutions filtered by {string}")
    public void theResponseShouldContainAListOfPaginatedInsitutionsFilteredBy(String arg0) {

    }

    @And("the paginated response contains institutions only for {string}")
    public void thePaginatedResponseContainsInstitutionOnlyFor(String productId) {
        Assertions.assertTrue(dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().stream()
                .allMatch(delegation -> delegation.getProductId().equals(productId)));
    }


    @And("the Institution Resource response should contain an institution id")
    public void theInstitutionResourceResponseShouldContainAInstitutionId() {
        Assertions.assertTrue(StringUtils.isNotBlank(dashboardStepsUtil.responses.getInstitutionResource().getId()));
        Assertions.assertEquals(dashboardStepsUtil.responses.getInstitutionResource().getId(), dashboardStepsUtil.filter.getInstitutionId());
    }

    @And("the Institution response should contain an institution id")
    public void theInstitutionResponseShouldContainAInstitutionId() {
        Assertions.assertTrue(StringUtils.isNotBlank(dashboardStepsUtil.responses.getInstitution().getId()));
        Assertions.assertEquals(dashboardStepsUtil.responses.getInstitution().getId(), dashboardStepsUtil.filter.getInstitutionId());
    }

    @And("the response should contain a list of paginated institutions with other pages")
    public void theResponseShouldContainAListOfPaginatedInstitutionsWithOtherPages() {
        Assertions.assertEquals(2, dashboardStepsUtil.responses.getDelegationWithPagination().getPageInfo().getTotalElements());
        Assertions.assertEquals(1, dashboardStepsUtil.responses.getDelegationWithPagination().getDelegations().size());
        Assertions.assertEquals(2, dashboardStepsUtil.responses.getDelegationWithPagination().getPageInfo().getTotalPages());
        Assertions.assertEquals(0, dashboardStepsUtil.responses.getDelegationWithPagination().getPageInfo().getPageNo());
    }

    @And("the response should contain userId")
    public void theResponseShouldContainUserId() {
        Assertions.assertTrue(StringUtils.isNotBlank(dashboardStepsUtil.responses.getInstitutionUserDetailsResource().getId().toString()));
        Assertions.assertEquals(dashboardStepsUtil.responses.getInstitutionUserDetailsResource().getId().toString(), dashboardStepsUtil.filter.getUserId());
    }

    @When("I send a GET request to {string} to retrieve institution user")
    public void iSendAGETRequestToToRetrieveInstitutionUser(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .pathParam("userId", dashboardStepsUtil.filter.getUserId())
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setInstitutionUserDetailsResource(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve institutions list")
    public void iSendAGETRequestToToRetrieveInstitutionsList(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if (dashboardStepsUtil.status == 200) {
            dashboardStepsUtil.responses.setInstitutionBaseResourceList(response.body().as(new TypeRef<>() {}));
        } else {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @And("the response should contain institutions list")
    public void theResponseShouldContainInstitutionsList() {
        Assertions.assertFalse(dashboardStepsUtil.responses.getInstitutionBaseResourceList().isEmpty());
        Assertions.assertEquals(6, dashboardStepsUtil.responses.getInstitutionBaseResourceList().size());
        Assertions.assertEquals("c9a50656-f345-4c81-84be-5b2474470544", dashboardStepsUtil.responses.getInstitutionBaseResourceList().get(0).getId());
        Assertions.assertEquals("467ac77d-7faa-47bf-a60e-38ea74bd5fd2", dashboardStepsUtil.responses.getInstitutionBaseResourceList().get(1).getId());
        Assertions.assertEquals("467ac77d-7faa-47bf-a60e-38ea74bd5fd2", dashboardStepsUtil.responses.getInstitutionBaseResourceList().get(2).getId());
        Assertions.assertEquals("0b56686d-3e25-4851-86c8-b9ba0d4fe301", dashboardStepsUtil.responses.getInstitutionBaseResourceList().get(3).getId());
        Assertions.assertEquals("f94c0589-b07e-4ee7-a509-fda5fe91faa2", dashboardStepsUtil.responses.getInstitutionBaseResourceList().get(4).getId());
        Assertions.assertEquals("067327d3-bdd6-408d-8655-87e8f1960046", dashboardStepsUtil.responses.getInstitutionBaseResourceList().get(5).getId());
    }

    @When("I send a PUT request to {string} to add a new user related to a product for institutions")
    public void iSendAPUTRequestToToAddANewUserRelatedToAProductForInstitutions(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)) {
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .pathParam("productId", dashboardStepsUtil.filter.getProductId())
                .body(dashboardStepsUtil.requests.getCreateUserDto())
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

    @And("the following user data request details:")
        public void theFollowingUserDataRequestDetails(List<CreateUserDto> createUserDtos) {
            if (createUserDtos != null && createUserDtos.size() == 1) {
                dashboardStepsUtil.requests.setCreateUserDto(createUserDtos.get(0));
            }
    }

    @And("the UserIdResource response should contain userId {string}")
    public void theUserIdResourceResponseShouldContainUserId(String userId) {
        Assertions.assertTrue(StringUtils.isNotBlank(dashboardStepsUtil.responses.getUserIdResource().getId().toString()));
        Assertions.assertEquals(dashboardStepsUtil.responses.getUserIdResource().getId().toString(), dashboardStepsUtil.filter.getUserId());
    }

}
