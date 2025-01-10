package it.pagopa.selfcare.dashboard.integration_test.steps;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import it.pagopa.selfcare.dashboard.model.user_groups.CreateUserGroupDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class UserApiSteps{

    @Autowired
    private DashboardStepsUtil dashboardStepsUtil;

    @When("I send a POST request to {string} to update user status")
    public void iSendAPOSTRequestTo(String url) {
    }

    @When("I send a DELETE request to {string} to delete user product")
    public void iSendADELETERequestToToDeleteUserProduct(String url) {
    }

    @When("I send a GET request to {string} to retrieve user data")
    public void iSendAGETRequestToToRetrieveUserData(String url) {
    }

    @When("I send a GET request to {string} to retrieve user product data")
    public void iSendAGETRequestToToRetrieveUserProductData(String url) {
    }

    @When("I send a POST request to {string} to retrieve user data from taxCode")
    public void iSendAPOSTRequestToToRetrieveUserDataFromTaxCode(String url) {
    }

    @When("I send a PUT request to {string} to update user data")
    public void iSendAPUTRequestTo(String url) {
    }

    @And("the user product should be {string} on each product roles")
    public void theUserShouldBeSuspendedOnEachProductRoles(String status) {
        //TODO: CHIAMATA ALLA GET E POI ASSERZIONI SUGLI STATI
    }

    @And("the user product should be {string} only on filtered product roles")
    public void theUserShouldBeSuspendedOnlyOnFilteredProductRoles(String status) {
        //TODO: CHIAMATA ALLA GET E POI ASSERZIONe sullo stato
    }

    @And("the user product should be {string}")
    public void theUserShouldBeSuspended(String status) {
        //TODO: CHIAMATA ALLA GET E POI ASSERZIONe sullo stato
    }

    @And("the user is removed from usergroup")
    public void theUserIsRemovedFromUsergroup() {
    }

    @And("the response should contain the user data")
    public void theResponseShouldContainTheUserData() {
        
    }

    @And("the response should contain only workContacts of user")
    public void theResponseShouldContainOnlyWorkContactsOfUser() {
        
    }

    @And("the response should contain the user data with  name, familyName, email, workContacts fields")
    public void theResponseShouldContainTheUserDataWithNameFamilyNameEmailWorkContactsFields() {

    }

    @And("the response should contain the user product data")
    public void theResponseShouldContainTheUserProductData() {

    }

    @And("the response should contain {int} items")
    public void theResponseShouldContainItems(int items) {
    }

    @And("the user email should be updated")
    public void theUserEmailShouldBeUpdated() {
        //TODO: GET USER BY ID PER VERIFICARE LA MAIL AGGIORNATA
    }

    @And("the user mobilePhone should be updated")
    public void theUserMobilePhoneShouldBeUpdated() {
        //TODO: GET USER BY ID PER VERIFICARE il TEL AGGIORNATO

    }
}
