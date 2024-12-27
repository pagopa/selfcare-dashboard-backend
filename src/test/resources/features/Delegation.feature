@exclude
Feature: Delegation

  Scenario: Attempt to createDelegation without permissions
    Given user login with username "r.balboa" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION | from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 401

  Scenario: Successfully creating a delegation with valid data
    Given user login with username "j.doe" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION | from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 201
    And the response should contain a delegation id

  Scenario: Creating a delegation with a missing "to" field
    Given user login with username "j.doe" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION | from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 400
    And the response should contain an error message "The 'to' field is required"

  Scenario: Creating a delegation with an invalid "to" field (non-existent tax code)
    Given user login with username "j.doe" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION |  from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 404
    And the response should contain an error message "Institution with tax code 'invalid-tax-code' not found"

  Scenario: Creating a delegation for the product "prod-pagopa" and setting the institution from the tax code
    Given user login with username "j.doe" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION |  from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 201
    And the response should contain a delegation id

  Scenario: Creating a delegation for a product that is not "prod-pagopa"
    Given user login with username "j.doe" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION |  from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 201
    And the response should contain a delegation id

  Scenario: Creating a delegation with a conflict (duplicate delegation)
    Given user login with username "j.doe" and password "test"
    And the following delegation request details:
      |Api               | from         | to  | productId  | type | institutionToName | institutionFromName
      |CREATE_DELEGATION |  from         | to  | productId  | type | institutionToName | institutionFromName
    When I send a POST request to "/v1/delegations" to create a delegation
    Then the response status should be 409
    And the response should contain an error message "Conflict"

  Scenario: Attempt to retrieve delegation with delegator without permissions
    Given user login with username "r.balboa" and password "test"
    When I send a GET request to "/v1/institutions/{institutionId}/partners" to retrieve delegations
    Then the response status should be 401

  Scenario: Successfully retrieve delegation using delegator without filter for productId
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v1/institutions/{institutionId}/partners" to retrieve delegations
    Then the response status should be 200
    And the response should contain a list of delegations

  Scenario: Successfully retrieve delegation using delegator with filter for productId
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    When I send a GET request to "/v1/institutions/{institutionId}/partners" to retrieve delegations
    Then the response status should be 200
    And the response should contain a list of delegations
    And the response contains delegation only for "prod-interop"

  Scenario: Attempt to retrieve delegation using delegator with filter for productId nothing found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-io"
    When I send a GET request to "/v1/institutions/{institutionId}/partners" to retrieve delegations
    Then the response status should be 200
    And the response should contain an empty list

  Scenario: Successfully retrieve delegation using delegate without filter for productId
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v1/institutions/{institutionId}/institutions" to retrieve delegations
    Then the response status should be 200
    And the response should contain a list of delegations

  Scenario: Attempt to retrieve delegation with delegate without permissions
    Given user login with username "r.balboa" and password "test"
    When I send a GET request to "/v1/institutions/{institutionId}/institutions" to retrieve delegations
    Then the response status should be 401

  Scenario: Successfully retrieve delegation using delegate with filter for productId
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    When I send a GET request to "/v1/institutions/{institutionId}/institutions" to retrieve delegations
    Then the response status should be 200
    And the response should contain a list of delegations
    And the response contains delegation only for "prod-interop"

  Scenario: Attempt to retrieve delegation using delegate with filter for productId nothing found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-io"
    When I send a GET request to "/v1/institutions/{institutionId}/institutions" to retrieve delegations
    Then the response status should be 200
    And the response should contain an empty list

  Scenario: Successfully retrieve paginated delegation using delegate without filter for productId
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 200
    And the response should contain a list of paginated delegations

  Scenario: Attempt to retrieve paginated delegation with delegate without permissions
    Given user login with username "r.balboa" and password "test"
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 401

  Scenario: Successfully retrieve paginated delegation using delegate with all filters
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    And I have "search" as search filter
    And require page 0 and size 10 and order by "asc"
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 200
    And the response should contain a list of paginated delegations

  Scenario: Successfully retrieve paginated delegation using delegate with filter for productId
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 200
    And the response should contain a list of paginated delegations
    And the paginated response contains delegation only for "prod-interop"

  Scenario: Successfully retrieve paginated delegation using delegate with search filter
    Given user login with username "j.doe" and password "test"
    And I have "search" as search filter
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 200
    And the response should contain a list of paginated delegations filtered by "search"

  Scenario: Successfully retrieve paginated delegation using delegate with order filter
    Given user login with username "j.doe" and password "test"
    And I have "ASC" as order
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 200
    And the response should contain a list of paginated delegations
    And the response should be ordered by "ASC"

  Scenario: Successfully retrieve paginated delegation using delegate with invalid size filter
    Given user login with username "j.doe" and password "test"
    And I have "0" as size filter
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 400
    And the response should contain an error message ""

  Scenario: Attempt to retrieve paginated delegation using delegate with filter for productId nothing found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-io"
    When I send a GET request to "/v2/institutions/{institutionId}/institutions" to retrieve paginated delegations
    Then the response status should be 200
    And the response should contain an empty list
