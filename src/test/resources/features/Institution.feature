@exclude
Feature: institution

  Scenario: Successfully save institution logo by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a PUT request to "/v1/institutions/{institutionId}/logo" to save institutions logo
    Then the response status should be 200

  Scenario: Attempt to save institution logo by a nonexistent institutionId
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960044"
    When I send a PUT request to "/v1/institutions/{institutionId}/logo" to save institutions logo
    Then the response status should be 404

  Scenario: Attempt to save institution logo by institutionId without permissions
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a PUT request to "/v1/institutions/{institutionId}/logo" to save institutions logo
    Then the response status should be 404

  Scenario: Successfully retrieve institution by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v1/institutions/{institutionId}" to retrieve institutions
    Then the response status should be 200
    And the Institution Resource response should contain an institution id

  Scenario: Attempt to retrieve institution by institutionId without permissions
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v1/institutions/{institutionId}" to retrieve institutions
    Then the response status should be 404

  Scenario: Attempt to retrieve nonexistent institution by institutionId
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960044"
    When I send a GET request to "/v1/institutions/{institutionId}" to retrieve delegations
    Then the response status should be 404

  @exclude
  Scenario: Successfully update institution geo-taxonomy by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following geo-taxonomy request details:
      | code   | desc
      | 058091 | ROMA - COMUNE
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 200

  @exclude
  Scenario: Attempt to update institution geo-taxonomy by institutionId with invalid request body
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following geo-taxonomy request details:
      | code   | desc
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 400

  @exclude
  Scenario: Attempt to update institution geo-taxonomy by a nonexistent institutionId
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960044"
    And the following geo-taxonomy request details:
      | code   | desc
      | 058091 | ROMA - COMUNE
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 404

  @exclude
  Scenario: Attempt to update institution geo-taxonomy by institutionId without permissions
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following geo-taxonomy request details:
      | code   | desc
      | 058091 | ROMA - COMUNE
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 404

  Scenario: Successfully update institution description by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following institution description request details:
      | description        | digitalAddress
      | comune di dernice2 | test@test.it
    When I send a PUT request to "/v1/institutions/{institutionId}" to update institution description
    Then the response status should be 200
    And the Institution response should contain an institution id

  Scenario: Attempt to update institution description by a nonexistent institutionId
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960044"
    And the following institution description request details:
      | description        | digitalAddress
      | comune di dernice2 | test@test.it
    When I send a PUT request to "/v1/institutions/{institutionId}" to update institution description
    Then the response status should be 404

  Scenario: Attempt to update institution description by institutionId without permissions
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following institution description request details:
      | description        | digitalAddress
      | comune di dernice2 | test@test.it
    When I send a PUT request to "/v1/institutions/{institutionId}" to update institution description
    Then the response status should be 404

  Scenario: Attempt to update institution description by institutionId with invalid request body
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following institution description request details:
      | description        | digitalAddress
    When I send a PUT request to "/v1/institutions/{institutionId}" to update institution description
    Then the response status should be 400

  Scenario: Successfully retrieve institution user by institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 200
    And the response should contain userId

  Scenario: Attempt to retrieve nonexistent institution user by institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "17a511a7-2acc-47b9-afed-2f3c65853b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 404

  Scenario: Attempt to retrieve institution user by institutionId without permissions
    Given user login with username "r.balboa" and password "test"
    And the userId is "17a511a7-2acc-47b9-afed-2f3c65853b4a"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 404

  Scenario: Successfully retrieve institutions
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v2/institutions" to retrieve institutions list
    Then the response status should be 200
    And the response should contain institutions list

  Scenario: Successfully create user product by institutionId and productId
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role     | productRoles        |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | OPERATOR | referente operativo |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 201

  Scenario: Attempt to create user product by a user by institutionId and with an existent different role
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role     | productRoles        |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | OPERATOR | referente operativo |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 400

  Scenario: Attempt to create user product by a user by institutionId and not allowed productId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role         | productRoles |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | SUB_DELEGATE | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 400

  Scenario: Attempt to create user product by a nonexistent institutionId and productId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1970046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role         | productRoles |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | SUB_DELEGATE | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 404

  Scenario: Attempt to create user product by institutionId and productId without permissions
    Given user login with username "j.doe" and password "test"
    And the institutionId is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2"
    And the productId is "prod-interop"
    And the following user data request details:
      | name | surname | taxCode          | email      | role     | productRoles        |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | OPERATOR | referente operativo |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 403

  Scenario: Attempt to create user product by institutionId and productId with invalid role
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role | productRoles |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | test | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 400
    And the response should contain an error message "Invalid role: test. Allowed values are: [MANAGER, DELEGATE, SUB_DELEGATE, OPERATOR, ADMIN_EA, ADMIN_EA_IO]"

  Scenario: Attempt to create user product by institutionId and productId with invalid request body
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode | email      | role         | productRoles |
      | john | Doe     |         | jd@test.it | SUB_DELEGATE | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to add a new user related to a product for institutions
    Then the response status should be 400
    And the response should contain an error message "taxCode,must not be blank"


  Scenario: Successfully create user product by institutionId and productId v2
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the following user data request details:
      | description        | digitalAddress
      | comune di dernice2 | test@test.it
    When I send a POST request to "/{institutionId}/products/{productId}/users/{userId}" to add a new user related to a product for institutions
    Then the response status should be 200
    
  Scenario: Attempt to create by a nonexistent institutionId and productId

  Scenario: Attempt to create user product by institutionId and productId without permissions

  Scenario: Attempt to create user product by institutionId and productId with invalid request body

  Scenario: Successfully retrieve institution by institutionId v2
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v2/institutions/{institutionId}" to retrieve institutions
    Then the response status should be 200
    And the Institution Resource response should contain an institution id









