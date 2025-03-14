Feature: Institution

  Scenario: Successfully save institution logo by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a PUT request to "/v1/institutions/{institutionId}/logo" to save institutions logo
    Then the response status should be 200

  Scenario: Attempt to save institution logo by institutionId without permissions (not onboarded)
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a PUT request to "/v1/institutions/{institutionId}/logo" to save institutions logo
    Then the response status should be 404

  Scenario: Successfully retrieve institution by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v1/institutions/{institutionId}" to retrieve institution
    Then the response status should be 200
    And The response body contains:
      | id                   | 067327d3-bdd6-408d-8655-87e8f1960046  |
      | externalId           | 99000870064                           |
      | originId             | c_d277                                |
      | origin               | IPA                                   |
      | institutionType      | PT                                    |
      | name                 | comune di dernice                     |
      | category             | Comuni e loro Consorzi e Associazioni |
      | categoryCode         | L6                                    |
      | fiscalCode           | 99000870064                           |
      | mailAddress          | protocollo@pec.comune.dernice.al.it   |
      | address              | Via Roma N.17                         |
      | zipCode              | 15056                                 |
    And The response body contains the list "products" of size 4

  Scenario: Attempt to retrieve institution by institutionId without permissions (not onboarded)
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v1/institutions/{institutionId}" to retrieve institution
    Then the response status should be 404

  Scenario: Successfully retrieve institution by institutionId v2
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v2/institutions/{institutionId}" to retrieve institution
    Then the response status should be 200
    And The response body contains:
      | id                   | 067327d3-bdd6-408d-8655-87e8f1960046  |
      | externalId           | 99000870064                           |
      | originId             | c_d277                                |
      | origin               | IPA                                   |
      | institutionType      | PT                                    |
      | name                 | comune di dernice                     |
      | category             | Comuni e loro Consorzi e Associazioni |
      | categoryCode         | L6                                    |
      | fiscalCode           | 99000870064                           |
      | mailAddress          | protocollo@pec.comune.dernice.al.it   |
      | address              | Via Roma N.17                         |
      | zipCode              | 15056                                 |
    And The response body contains the list "products" of size 4

  Scenario: Attempt to retrieve institution by institutionId v2 without permission (not onboarded)
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v2/institutions/{institutionId}" to retrieve institution
    Then the response status should be 404


  Scenario: Successfully update institution geo-taxonomy by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following geo-taxonomy request details:
      | code   | desc |
      | 058091 | ROMA |
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 200
    And I send a GET request to "/v1/institutions/{institutionId}" to retrieve institution
    And The response body contains:
      | geographicTaxonomies | [[code:058091, desc:ROMA - COMUNE]] |

  Scenario: Attempt to update institution geo-taxonomy by institutionId with invalid request body
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following geo-taxonomy request details:
      | code   | desc |
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 400

  Scenario: Attempt to update institution geo-taxonomy by institutionId without permissions (not onboarded)
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following geo-taxonomy request details:
      | code   | desc          |
      | 058091 | ROMA - COMUNE |
    When I send a PUT request to "/v1/institutions/{institutionId}/geographic-taxonomy" to update institutions geo-taxonomy
    Then the response status should be 404

  Scenario: Successfully update institution description by institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following institution description request details:
      | description        | digitalAddress |
      | comune di dernice2 | test@test.it   |
    When I send a PUT request to "/v1/institutions/{institutionId}" to update institution description
    Then the response status should be 200
    And the Institution response should contain an institution id
    And I send a GET request to "/v1/institutions/{institutionId}" to retrieve institution
    And The response body contains:
      | name        | comune di dernice2 |
      | mailAddress | test@test.it       |

  Scenario: Attempt to update institution description by institutionId without permissions (not onboarded)
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the following institution description request details:
      | description        | digitalAddress |
      | comune di dernice2 | test@test.it   |
    When I send a PUT request to "/v1/institutions/{institutionId}" to update institution description
    Then the response status should be 404

  Scenario: Successfully retrieve institution user by institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 200
    And The response body contains:
      | id         | 97a511a7-2acc-47b9-afed-2f3c65753b4a |
      | name       | john                                 |
      | surname    | Doe                                  |
      | role       | ADMIN                                |
      | status     | ACTIVE                               |
      | fiscalCode | PRVTNT80A41H401T                     |
    And The response body contains the list "products" of size 4

  Scenario: Attempt to retrieve institution user by institutionId without permissions (not onboarded)
    Given user login with username "j.doe" and password "test"
    And the userId is "17a511a7-2acc-47b9-afed-2f3c65853b4a"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    When I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 404

  Scenario: Attempt to retrieve institution user by institutionId without permissions (OPERATOR)
    Given user login with username "j.doe" and password "test"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the institutionId is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2"
    When I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 403

  Scenario: Successfully retrieve institutions
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v2/institutions" to retrieve institutions list
    Then the response status should be 200
    And the response should contain institutions list

  Scenario: Successfully retrieve empty institutions list
    Given user login with username "s.froid" and password "test"
    When I send a GET request to "/v2/institutions" to retrieve institutions list
    Then the response status should be 200
    And the response should contain an empty institutions list

  Scenario: Attempt to create user product by a user by institutionId and with an existent different role
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role     | productRoles        |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | OPERATOR | referente operativo |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to create a new user related to a product for institutions
    Then the response status should be 400

  Scenario: Attempt to create user product by a user by institutionId and not allowed productId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role         | productRoles |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | SUB_DELEGATE | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to create a new user related to a product for institutions
    Then the response status should be 400

  Scenario: Attempt to create user product by an institutionId and productId without permission (not onboarded)
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1970046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role         | productRoles |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | SUB_DELEGATE | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to create a new user related to a product for institutions
    Then the response status should be 404

  Scenario: Attempt to create user product by institutionId and productId without permissions (OPERATOR)
    Given user login with username "j.doe" and password "test"
    And the institutionId is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2"
    And the productId is "prod-interop"
    And the following user data request details:
      | name | surname | taxCode          | email      | role     | productRoles        |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | OPERATOR | referente operativo |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to create a new user related to a product for institutions
    Then the response status should be 403

  Scenario: Attempt to create user product by institutionId and productId with invalid role
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode          | email      | role | productRoles |
      | john | Doe     | PRVTNT80A41H401T | jd@test.it | test | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to create a new user related to a product for institutions
    Then the response status should be 400
    And the response should contain an error message "Invalid role: test. Allowed values are: [MANAGER, DELEGATE, SUB_DELEGATE, OPERATOR, ADMIN_EA]"

  Scenario: Attempt to create user product by institutionId and productId with invalid request body
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the following user data request details:
      | name | surname | taxCode | email      | role         | productRoles |
      | john | Doe     |         | jd@test.it | SUB_DELEGATE | admin        |
    When I send a POST request to "/v2/institutions/{institutionId}/products/{productId}/users" to create a new user related to a product for institutions
    Then the response status should be 400
    And the response should contain an error message "taxCode,must not be blank"

  Scenario: Successfully add user by institutionId, productId and userId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productId is "prod-interop"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the following user product request details:
      | role     | productRoles |
      | OPERATOR | security     |
    When I send a PUT request to "/v2/institutions/{institutionId}/products/{productId}/users/{userId}" to add a new user related to a product for institutions
    Then the response status should be 201
    And I send a GET request to "/v2/institutions/{institutionId}/users/{userId}" to retrieve institution user
    Then the response status should be 200
    And The response body contains:
      | id         | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7 |
    And The response body contains the list "products" of size 2

  Scenario: Attempt to add user by institutionId, productId and userId without permission
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-interop"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the following user product request details:
      | role     | productRoles |
      | DELEGATE | admin        |
    When I send a PUT request to "/v2/institutions/{institutionId}/products/{productId}/users/{userId}" to add a new user related to a product for institutions
    Then the response status should be 403

  Scenario: Successfully add user by institutionId, productId and userId not allowed product
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the following user product request details:
      | role     | productRoles |
      | DELEGATE | admin        |
    When I send a PUT request to "/v2/institutions/{institutionId}/products/{productId}/users/{userId}" to add a new user related to a product for institutions
    Then the response status should be 400
    
  Scenario: Attempt to add user with an nonexistent institutionId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1970046"
    And the productId is "prod-io"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the following user product request details:
      | role     | productRoles |
      | DELEGATE | admin        |
    When I send a PUT request to "/v2/institutions/{institutionId}/products/{productId}/users/{userId}" to add a new user related to a product for institutions
    Then the response status should be 404

  Scenario: Attempt to add user by institutionId, productId and userId with invalid request body
    Given user login with username "j.doe" and password "test"
    And the institutionId is "067327d3-bdd6-408d-8655-87e8f1960046"
    And the productId is "prod-io"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the following user product request details:
      | role     | productRoles |
      | DELEGATE |              |
    When I send a PUT request to "/v2/institutions/{institutionId}/products/{productId}/users/{userId}" to add a new user related to a product for institutions
    Then the response status should be 400

  Scenario: Successfully retrieve institution onboardings PENDING
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    And the fiscalCode is "08875230016"
    When I send a GET request to "/v2/institutions/onboardings/{productId}/pending" to pending onboardings
    Then the response status should be 200

  Scenario: Attempt to retrieve institution onboardings PENDING not found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-io"
    And the fiscalCode is "08875230016"
    When I send a GET request to "/v2/institutions/onboardings/{productId}/pending" to pending onboardings
    Then the response status should be 204

  Scenario: Successfully get User Count
    Given user login with username "j.doe" and password "test"
    And the institution ID is "c9a50656-f345-4c81-84be-5b2474470544" and the product ID is "prod-pagopa"
    When I send a GET request to "/v2/institutions/{institutionId}/products/{productId}/users/count" to get users count
    Then the response status should be 200
    And The response body contains:
      | count | 2 |

  Scenario: Attempt to get User Count without permission (OPERATOR)
    Given user login with username "j.doe" and password "test"
    And the institution ID is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2" and the product ID is "prod-interop"
    When I send a GET request to "/v2/institutions/{institutionId}/products/{productId}/users/count" to get users count
    Then the response status should be 403

  Scenario: Attempt to get User Count institution without permission (not onboarded)
    Given user login with username "r.balboa" and password "test"
    And the institution ID is "067327d3-bdd6-408d-8655-87e8f1960046" and the product ID is "prod-interop"
    When I send a GET request to "/v2/institutions/{institutionId}/products/{productId}/users/count" to get users count
    Then the response status should be 404