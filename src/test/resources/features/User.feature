Feature: User
  Scenario: Successfully suspend user with 2 roles on prod-interop without productRole filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-interop"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/{userId}/suspend" to update user status
    Then the response status should be 204

  Scenario: Successfully activate user with 2 roles on prod-interop without productRole filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-interop"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/{userId}/activate" to update user status
    Then the response status should be 204

  Scenario: Successfully suspend user with 2 roles on prod-interop with productRole filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-interop"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productRoles are "api"
    When I send a POST request to "/v2/users/{userId}/suspend" to update user status
    Then the response status should be 204
    And the user product should be "SUSPENDED" only on filtered product roles


  Scenario: Successfully activate user with 2 roles on prod-interop with productRole filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-interop"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productRoles are "api"
    When I send a POST request to "/v2/users/{userId}/activate" to update user status
    Then the response status should be 204
    And the user product should be "ACTIVE" only on filtered product roles

  Scenario: Successfully suspend user on prod-io
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-io"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/{userId}/suspend" to update user status
    Then the response status should be 204
    And the user product should be "SUSPENDED"

  Scenario: Try to suspend user with given userId without required filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/{userId}/suspend" to update user status
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'productId' for method parameter type String is not present"

  Scenario: Successfully activate user on prod-io
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-io"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/{userId}/activate" to update user status
    Then the response status should be 204
    And the user product should be "ACTIVE"

  Scenario: Try to activate user with given userId without required filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/{userId}/activate" to update user status
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'productId' for method parameter type String is not present"

  Scenario: Successfully delete user with 2 roles on prod-interop with productRole filter
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-interop"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productRoles are "security"
    When I send a DELETE request to "/v2/users/{userId}" to delete user product
    Then the response status should be 204
    And the user product should be "DELETED" only on filtered product roles

  Scenario: Successfully delete user on prod-io when user is member of a group
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-io"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And I have groupId "6759f8df78b6af202b222d29"
    And I send a POST request to "/v2/user-groups/{id}/members/{userId}" to add userGroup member
    When I send a DELETE request to "/v2/users/{userId}" to delete user product
    Then the response status should be 204
    And the user product should be "DELETED"
    And the user is removed from user group

  Scenario: Successfully delete user on prod-pagopa when user is not member of a group
    Given user login with username "j.doe" and password "test"
    And the userId is "4a7be530-1c36-41f4-8967-c479fb2d7fa9"
    And the productId is "prod-pagopa"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a DELETE request to "/v2/users/{userId}" to delete user product
    Then the response status should be 204
    And the user product should be "DELETED"

  Scenario: Try to delete user with given userId without required filter
    Given user login with username "j.doe" and password "test"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf7"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a DELETE request to "/v2/users/{userId}" to delete user product
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'productId' for method parameter type String is not present"


  Scenario: Successfully retrieve user data for given userId and institutionId without fields filter
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/users/{id}" to retrieve user data
    Then the response status should be 200
    And the response should contain the user data


  Scenario: Retrieving user data for given userId and institutionId without fields filter, user not found
    Given user login with username "j.doe" and password "test"
    And the userId is "35a78332-d038-4bfa-8e85-2cba7f6b7bf8"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/users/{id}" to retrieve user data
    Then the response status should be 404


  Scenario: Retrieving user data for given userId without institutionId filter
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    When I send a GET request to "/v2/users/{id}" to retrieve user data
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'institutionId' for method parameter type String is not present"


  Scenario: Successfully retrieve user data for given userId and institutionId with workContacts fields filter
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the fields are "name,familyName"
    When I send a GET request to "/v2/users/{id}" to retrieve user data
    Then the response status should be 200
    And the response should contain only name and familyName of user


  Scenario: Successfully retrieve user data for given userId and institutionId with name, familyName, email, workContacts fields filter
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the fields are "name,familyName,email,workContacts"
    When I send a GET request to "/v2/users/{id}" to retrieve user data
    Then the response status should be 200
    And the response should contain the user data with  name, familyName, email, workContacts fields


  Scenario: Successfully retrieve user data for given fiscalCode and institutionId
    Given user login with username "j.doe" and password "test"
    And the fiscalCode is "PRVTNT80A41H401T"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/search" to retrieve user data from taxCode
    Then the response status should be 200
    And the response should contain the user data


  Scenario: Retrieving user data for given fiscalCode without institutionId filter
    Given user login with username "j.doe" and password "test"
    And the fiscalCode is "PRVTNT80A41H401T"
    When I send a POST request to "/v2/users/search" to retrieve user data from taxCode
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'institutionId' for method parameter type String is not present"


  Scenario: Retrieving user data for given fiscalCode with institutionId filter, not found
    Given user login with username "j.doe" and password "test"
    And the fiscalCode is "CCCCCC80C05C525C"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a POST request to "/v2/users/search" to retrieve user data from taxCode
    Then the response status should be 404

  Scenario: Successfully retrieve users for given institutionId without filters
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/users/institution/{institutionId}" to retrieve user product data
    Then the response status should be 200
    And the response should contain 6 items


  Scenario: Successfully retrieve users for given institutionId with productRoles filter
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productRoles are "admin,security"
    When I send a GET request to "/v2/users/institution/{institutionId}" to retrieve user product data
    Then the response status should be 200
    And the response should contain 5 items

  Scenario: Successfully retrieve users for given institutionId with productId filter
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productId is "prod-io"
    When I send a GET request to "/v2/users/institution/{institutionId}" to retrieve user product data
    Then the response status should be 200
    And the response should contain 2 items

  Scenario: Successfully retrieve users for given institutionId with productId, roles and productRoles filters
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productId is "prod-interop"
    And the roles are "OPERATOR"
    And the productRoles are "api"
    When I send a GET request to "/v2/users/institution/{institutionId}" to retrieve user product data
    Then the response status should be 200
    And the response should contain 1 items

  Scenario: Successfully retrieve users for given institutionId with productId, roles and productRoles filters, no users found
    Given user login with username "j.doe" and password "test"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the productId is "prod-io"
    And the roles are "OPERATOR"
    And the productRoles are "operator"
    When I send a GET request to "/v2/users/institution/{institutionId}" to retrieve user product data
    Then the response status should be 200
    And the response should contain 0 items

  Scenario: Successfully update user email for given userId and institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the email is "cucumber@test.it"
    And the mobilePhone is "3252525251"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 204
    And the user email should be updated

  Scenario: Successfully update user mobilePhone for given userId and institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the email is "cucumber@test.it"
    And the mobilePhone is "3252525252"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 204
    And the user mobilePhone should be updated

  Scenario: Successfully update user mobilePhone and email for given userId and institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the email is "cucumber2@test.it"
    And the mobilePhone is "3252525253"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 204
    And the user mobilePhone should be updated

  Scenario: Updating user email for given userId without institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the email is "cucumber@test.it"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 400
    And the response should contain an error message "Required request parameter 'institutionId' for method parameter type String is not present"

  Scenario: Updating user email for given userId with non existent institutionId
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470555"
    And the email is "cucumber@test.it"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 404

  Scenario: Updating user with mobilePhone with less than 7 characters
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the mobilePhone is "12345"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 400
    And the response should contain an error message "Il numero di telefono non è valido"

  Scenario: Updating user with mobilePhone with more than 15 characters
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the mobilePhone is "12345678910111213"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 400
    And the response should contain an error message "Il numero di telefono non è valido"

  Scenario: Updating user with mobilePhone with alphanumeric characters
    Given user login with username "j.doe" and password "test"
    And the userId is "97a511a7-2acc-47b9-afed-2f3c65753b4a"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    And the mobilePhone is "num123456"
    When I send a PUT request to "/v2/users/{id}" to update user data
    Then the response status should be 400
    And the response should contain an error message "Il numero di telefono non è valido"





