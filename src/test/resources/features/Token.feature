@token
Feature: Token API

  ##token exchange
  Scenario: Successful token exchange with valid institution and product IDs
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productId is "prod-interop"
    When I send a GET request to "/v2/token/exchange" with the given details to retrieve token
    Then the response status should be 200
    And the response should contain a valid token

  Scenario: Attempt to token exchange without institution ID
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    When I send a GET request to "/v2/token/exchange" with the given details to retrieve token
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'institutionId' for method parameter type String is not present"

  Scenario: Attempt to token exchange without productId
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/token/exchange" with the given details to retrieve token
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'productId' for method parameter type String is not present"

  Scenario: Attempt to retrieve token without user permissions on product
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2"
    And the productId is "prod-interop"
    When I send a GET request to "/v2/token/exchange" with the given details to retrieve token
    Then the response status should be 404

  ##billing token exchange
  Scenario: Successful billing token exchange with valid institution ID and environment
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/token/exchange/fatturazione" with the given details to retrieve billing token
    Then the response status should be 200
    And the response should contain a valid token

  Scenario: Attempt to retrieve billing token without institution ID
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v2/token/exchange/fatturazione" with the given details to retrieve billing token
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'institutionId' for method parameter type String is not present"



