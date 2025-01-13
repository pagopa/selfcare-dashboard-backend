package it.pagopa.selfcare.dashboard.integration_test.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.specification.RequestSpecification;
import it.pagopa.selfcare.dashboard.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.model.user.UserResource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.mongodb.assertions.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserApiSteps{

    @Autowired
    private DashboardStepsUtil dashboardStepsUtil;

    @When("I send a POST request to {string} to update user status")
    public void iSendAPOSTRequestTo(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)){
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        if(StringUtils.isNotBlank(dashboardStepsUtil.filter.getProductId())){
            requestSpecification.queryParam("productId", dashboardStepsUtil.filter.getProductId());
        }
        if (StringUtils.isNotBlank(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }
        if(!CollectionUtils.isEmpty(dashboardStepsUtil.filter.getProductRoles())){
            requestSpecification.queryParam("productRole", dashboardStepsUtil.filter.getProductRoles().get(0));
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("userId", dashboardStepsUtil.filter.getUserId())
                .post(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if(dashboardStepsUtil.status != 204) {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a DELETE request to {string} to delete user product")
    public void iSendADELETERequestToToDeleteUserProduct(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)){
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        if(StringUtils.isNotBlank(dashboardStepsUtil.filter.getProductId())){
            requestSpecification.queryParam("productId", dashboardStepsUtil.filter.getProductId());
        }
        if (StringUtils.isNotBlank(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }
        if(!CollectionUtils.isEmpty(dashboardStepsUtil.filter.getProductRoles())){
            requestSpecification.queryParam("productRole", dashboardStepsUtil.filter.getProductRoles().get(0));
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("userId", dashboardStepsUtil.filter.getUserId())
                .delete(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if(dashboardStepsUtil.status != 204) {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve user data")
    public void iSendAGETRequestToToRetrieveUserData(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)){
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }
        if (StringUtils.isNotBlank(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }
        if(!CollectionUtils.isEmpty(dashboardStepsUtil.filter.getFields())){
            requestSpecification.queryParam("fields", dashboardStepsUtil.filter.getFields());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("id", dashboardStepsUtil.filter.getUserId())
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if(dashboardStepsUtil.status == 200){
            dashboardStepsUtil.responses.setUserResource(response.as(UserResource.class));
        }else{
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a GET request to {string} to retrieve user product data")
    public void iSendAGETRequestToToRetrieveUserProductData(String url) {
        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)){
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        if(StringUtils.isNotBlank(dashboardStepsUtil.filter.getProductId())){
            requestSpecification.queryParam("productId", dashboardStepsUtil.filter.getProductId());
        }
        if(!CollectionUtils.isEmpty(dashboardStepsUtil.filter.getRoles())){
            requestSpecification.queryParam("roles", dashboardStepsUtil.filter.getRoles());
        }
        if(!CollectionUtils.isEmpty(dashboardStepsUtil.filter.getProductRoles())){
            requestSpecification.queryParam("productRoles", dashboardStepsUtil.filter.getProductRoles());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("institutionId", dashboardStepsUtil.filter.getInstitutionId())
                .get(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if(dashboardStepsUtil.status == 200){
            dashboardStepsUtil.responses.setProductUserResource(response.as(new TypeRef<>() {}));
        }else{
            dashboardStepsUtil.errorMessage = response.body().asString();
        }

    }

    @When("I send a POST request to {string} to retrieve user data from taxCode")
    public void iSendAPOSTRequestToToRetrieveUserDataFromTaxCode(String url) {

        SearchUserDto searchUserDto = new SearchUserDto();

        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)){
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        if(StringUtils.isNotBlank(dashboardStepsUtil.filter.getTaxCode())){
            searchUserDto.setFiscalCode(dashboardStepsUtil.filter.getTaxCode());
        }
        if (StringUtils.isNotBlank(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .body(searchUserDto)
                .post(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if(dashboardStepsUtil.status == 200){
            dashboardStepsUtil.responses.setUserResource(response.as(UserResource.class));
        }else{
            dashboardStepsUtil.errorMessage = response.body().asString();
        }
    }

    @When("I send a PUT request to {string} to update user data")
    public void iSendAPUTRequestTo(String url) {
        UpdateUserDto updateUserDto = new UpdateUserDto();

        RequestSpecification requestSpecification = RestAssured.given()
                .contentType("application/json");

        if(StringUtils.isNotBlank(dashboardStepsUtil.token)){
            requestSpecification.header("Authorization", "Bearer " + dashboardStepsUtil.token);
        }

        if(StringUtils.isNotBlank(dashboardStepsUtil.filter.getEmail())){
            updateUserDto.setEmail(dashboardStepsUtil.filter.getEmail());
        }
        if(StringUtils.isNotBlank(dashboardStepsUtil.filter.getMobilePhone())){
            updateUserDto.setMobilePhone(dashboardStepsUtil.filter.getMobilePhone());
        }

        if (StringUtils.isNotBlank(dashboardStepsUtil.filter.getInstitutionId())) {
            requestSpecification.queryParam("institutionId", dashboardStepsUtil.filter.getInstitutionId());
        }

        ExtractableResponse<?> response = requestSpecification
                .when()
                .pathParam("id", dashboardStepsUtil.filter.getUserId())
                .body(updateUserDto)
                .put(url)
                .then()
                .extract();

        dashboardStepsUtil.status = response.statusCode();
        if(dashboardStepsUtil.status != 204) {
            dashboardStepsUtil.errorMessage = response.body().asString();
        }

    }

    @And("the user product should be {string} on each product roles")
    public void theUserShouldBeSuspendedOnEachProductRoles(String status) {
        //TODO: QUANDO SARA IMPLEMENTATA LA CHIAMATA PER LA GET USER INSTITUTION, DOVRA' ESSERE INVOCATO IL METODO CHE FA TALE CHIAMATA E DOVRANNO ESSERE
        //VERIFICATI SCRITTE LE ASSERZIONI SUGLI STATI DEI PRODOTTI
    }

    @And("the user product should be {string} only on filtered product roles")
    public void theUserShouldBeSuspendedOnlyOnFilteredProductRoles(String status) {
        //TODO: QUANDO SARA IMPLEMENTATA LA CHIAMATA PER LA GET USER INSTITUTION, DOVRA' ESSERE INVOCATO IL METODO CHE FA TALE CHIAMATA E DOVRANNO ESSERE
        //VERIFICATI SCRITTE LE ASSERZIONI SUGLI STATI DEI PRODOTTI
    }

    @And("the user product should be {string}")
    public void theUserShouldBeSuspended(String status) {
        //TODO: QUANDO SARA IMPLEMENTATA LA CHIAMATA PER LA GET USER INSTITUTION, DOVRA' ESSERE INVOCATO IL METODO CHE FA TALE CHIAMATA E DOVRANNO ESSERE
        //VERIFICATI SCRITTE LE ASSERZIONI SUGLI STATI DEI PRODOTTI
    }

    @And("the response should contain the user data")
    public void theResponseShouldContainTheUserData() {
        assertEquals(UUID.fromString("97a511a7-2acc-47b9-afed-2f3c65753b4a"),dashboardStepsUtil.responses.getUserResource().getId());
        assertEquals("john",dashboardStepsUtil.responses.getUserResource().getName().getValue());
        assertEquals("Doe",dashboardStepsUtil.responses.getUserResource().getFamilyName().getValue());
        assertNotEquals("personal@test.it",dashboardStepsUtil.responses.getUserResource().getEmail().getValue());
        assertEquals("3365252525",dashboardStepsUtil.responses.getUserResource().getMobilePhone().getValue());
    }

    @And("the response should contain only name and familyName of user")
    public void theResponseShouldContainOnlyNameAndFamilyNameOfUser() {
        assertEquals(UUID.fromString("97a511a7-2acc-47b9-afed-2f3c65753b4a"),dashboardStepsUtil.responses.getUserResource().getId());
        assertEquals("john",dashboardStepsUtil.responses.getUserResource().getName().getValue());
        assertNull(dashboardStepsUtil.responses.getUserResource().getFiscalCode());
        assertEquals("Doe", dashboardStepsUtil.responses.getUserResource().getFamilyName().getValue());
        assertNull(dashboardStepsUtil.responses.getUserResource().getEmail());
        assertNull(dashboardStepsUtil.responses.getUserResource().getMobilePhone());
    }

    @And("the response should contain the user data with  name, familyName, email, workContacts fields")
    public void theResponseShouldContainTheUserDataWithNameFamilyNameEmailWorkContactsFields() {
        assertEquals(UUID.fromString("97a511a7-2acc-47b9-afed-2f3c65753b4a"),dashboardStepsUtil.responses.getUserResource().getId());
        assertEquals("john",dashboardStepsUtil.responses.getUserResource().getName().getValue());
        assertEquals("Doe",dashboardStepsUtil.responses.getUserResource().getFamilyName().getValue());
        assertNull(dashboardStepsUtil.responses.getUserResource().getFiscalCode());
        assertNotEquals("personal@test.it",dashboardStepsUtil.responses.getUserResource().getEmail().getValue());
        assertEquals("3365252525",dashboardStepsUtil.responses.getUserResource().getMobilePhone().getValue());
    }

    @And("the response should contain {int} items")
    public void theResponseShouldContainItems(int items) {
        assertEquals(items, dashboardStepsUtil.responses.getProductUserResource().size());
    }

    @And("the user email should be updated")
    public void theUserEmailShouldBeUpdated() {
        //TODO: GET USER INSTITUTION CONTROLLARE CHE IL CAMPO userMailUpdatedAt SIA DIVERSO DA NULL E CHE IL CAMPO userMailUuid SIA DIVERSO DA ID_CONTACTS#8370aa38-a2ab-404b-9b8a-10487167332e
    }

    @And("the user mobilePhone should be updated")
    public void theUserMobilePhoneShouldBeUpdated() {
        //TODO: GET USER INSTITUTION CONTROLLARE CHE IL CAMPO userMailUpdatedAt SIA DIVERSO DA NULL E CHE IL CAMPO userMailUuid SIA DIVERSO DA ID_CONTACTS#8370aa38-a2ab-404b-9b8a-10487167332e
    }
}
