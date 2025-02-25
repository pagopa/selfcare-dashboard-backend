Feature: Product

  Scenario: Successfully retrieving the back-office URL for a valid product and institution
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pn"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/products/{productId}/back-office" to retrieve back-office URL
    Then the response status should be 200
    And the response should contain a back-office URL with selfcare token


  Scenario: Retrieving the back-office URL for a product without institutionId
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    When I send a GET request to "/v2/products/{productId}/back-office" to retrieve back-office URL
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'institutionId' for method parameter type String is not present"

  Scenario: Retrieving the back-office URL for a product with lang parameter
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pn"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the language is "de"
    When I send a GET request to "/v2/products/{productId}/back-office" to retrieve back-office URL
    Then the response status should be 200
    And the response should contain a back-office URL with selfcare token

  Scenario: Retrieving the back-office URL with an invalid environment parameter
    Given user login with username "j.doe" and password "test"
    And the productId is "product123"
    And the institutionId is "institution456"
    And the environment is "invalidEnv"
    When I send a GET request to "/v2/products/{productId}/back-office" to retrieve back-office URL
    Then the response status should be 404

  Scenario: Retrieving the back-office URL for a product and institution the user does not have permission to access
    Given user login with username "r.balboa" and password "test"
    And the productId is "product123"
    And the institutionId is "institution456"
    When I send a GET request to "/v2/products/{productId}/back-office" to retrieve back-office URL
    Then the response status should be 404

  Scenario: Successfully retrieving the product roles for given productId and institutionType
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pagopa"
    And the institutionType is "PSP"
    When I send a GET request to "/v1/products/{productId}/roles" to retrieve productRoles
    Then the response status should be 200
    And the response should contain a list of product roles
    And the response should contains "operator-psp" as productRole code

  Scenario: Successfully retrieving the product roles for given productId
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pagopa"
    When I send a GET request to "/v1/products/{productId}/roles" to retrieve productRoles
    Then the response status should be 200
    And the response should contain a list of product roles
    And the response should contains "operator" as productRole code

  Scenario: Successfully retrieving the product brokers for product PagoPA and institutionType PSP found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pagopa"
    And the institutionType is "PSP"
    When I send a GET request to "/v1/products/{productId}/brokers/{institutionType}" to retrieve product brokers
    Then the response status should be 200
    And the response should contain a list of pagopa psp product brokers

  Scenario: Successfully retrieving the product brokers for product PagoPA and institutionType PA found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pagopa"
    And the institutionType is "PA"
    When I send a GET request to "/v1/products/{productId}/brokers/{institutionType}" to retrieve product brokers
    Then the response status should be 200
    And the response should contain a list of pagopa product brokers

  Scenario: Successfully retrieving the product brokers for product PagoPA and institutionType PSP not found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pagopa"
    And the institutionType is "PSP"
    When I send a GET request to "/v1/products/{productId}/brokers/{institutionType}" to retrieve product brokers
    Then the response status should be 200
    And the response should not contain any product brokers

  Scenario: Successfully retrieving the product brokers for product PagoPA and institutionType PA not found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-pagopa"
    And the institutionType is "PA"
    When I send a GET request to "/v1/products/{productId}/brokers/{institutionType}" to retrieve product brokers
    Then the response status should be 200
    And the response should not contain any product brokers

  Scenario: Successfully retrieving the product brokers for product interop and given institutionType found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-interop"
    And the institutionType is "PSP"
    When I send a GET request to "/v1/products/{productId}/brokers/{institutionType}" to retrieve product brokers
    Then the response status should be 200
    And the response should contain a list of product brokers

  Scenario: Successfully retrieving the product brokers for product interop and given institutionType not found
    Given user login with username "j.doe" and password "test"
    And the productId is "prod-io-premium"
    And the institutionType is "PA"
    When I send a GET request to "/v1/products/{productId}/brokers/{institutionType}" to retrieve product brokers
    Then the response status should be 200
    And the response should not contain any product brokers


  Scenario: Successfully retrieving products tree
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v1/institutions/products" to retrieve products tree
    Then the response status should be 200



