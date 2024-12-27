@exclude
Feature: User

  Scenario: Attempt to suspend user without permissions
  Scenario: Attempt to delete user without permissions
  Scenario: Attempt to activate user without permissions
  Scenario: Attempt to retrieve by id user without permissions
  Scenario: Attempt to retrieve by taxCode user without permissions
  Scenario: Attempt to update user without permissions
  Scenario: Attempt to retrieve users without permissions

  Scenario: Successfully suspend user with 2 roles on prod-interop without productRole filter
    Given I have a valid userId and productId
    When I suspend the user
    Then the response status should be 200
    And the user should be suspended on each product roles

  Scenario: Successfully suspend user with 2 roles on prod-interop with productRole filter
    Given I have a valid userId, productId and productRole
    When I suspend the user with productRole filter
    Then the response status should be 200
    And the user should be suspended only on filtered product roles

  Scenario: Successfully suspend user on prod-io
    Given I have a valid userId and productId
    When I suspend the user
    Then the response status should be 200
    And the user should be suspended on the product

  Scenario: Try to suspend user with given userId without required filter
    Given I have a valid userId
    When I try to suspend the user without required filter
    Then the response status should be 400
    And the response should contain the error message "productId is required"

  Scenario: Successfully delete user with 2 roles on prod-interop without productRole filter
    Given I have a valid userId and productId
    When I delete the user
    Then the response status should be 200
    And the user should be deleted on each product roles

  Scenario: Successfully delete user with 2 roles on prod-interop with productRole filter
    Given I have a valid userId, productId and productRole
    When I delete the user with productRole filter
    Then the response status should be 200
    And the user should be deleted only on filtered product roles

  Scenario: Successfully delete user on prod-io when user is member of a group
    Given I have a valid userId and productId
    When I delete the user
    Then the response status should be 200
    And the user should be deleted on the product

  Scenario: Successfully delete user on prod-io when user is not member of a group
    Given I have a valid userId and productId
    When I delete the user
    Then the response status should be 200
    And the user should be deleted on the product

  Scenario: Try to delete user with given userId without required filter
    Given I have a valid userId
    When I try to delete the user without required filter
    Then the response status should be 400
    And the response should contain the error message "productId is required"

  Scenario: Successfully activate user with 2 roles on prod-interop without productRole filter
    Given I have a valid userId and productId
    When I activate the user
    Then the response status should be 200
    And the user should be activated on each product roles

  Scenario: Successfully activate user with 2 roles on prod-interop with productRole filter
    Given I have a valid userId, productId and productRole
    When I activate the user with productRole filter
    Then the response status should be 200
    And the user should be activated only on filtered product roles

  Scenario: Successfully activate user on prod-io
    Given I have a valid userId and productId
    When I activate the user
    Then the response status should be 200
    And the user should be activated on the product

  Scenario: Try to activate user with given userId without required filter
    Given I have a valid userId
    When I try to activate the user without required filter
    Then the response status should be 400
    And the response should contain the error message "productId is required"

  Scenario: Successfully retrieve user data for given userId and institutionId without fields filter
    Given I have a valid userId and institutionId
    When I retrieve the user data
    Then the response status should be 200
    And the response should contain the user data

  Scenario: Retrieving user data for given userId and institutionId without fields filter, user not found
    Given I have a valid userId and institutionId
    When I retrieve the user data
    Then the response status should be 404
    And the response should contain the error message "User not found"

  Scenario: Retrieving user data for given userId without institutionId filter
    Given I have a valid userId
    When I retrieve the user data
    Then the response status should be 400
    And the response should contain the error message "institutionId is required"

  Scenario: Successfully retrieve user data for given userId and institutionId with workContacts fields filter
    Given I have a valid userId and institutionId
    When I retrieve the user data with workContacts fields filter
    Then the response status should be 200
    And the response should contain the user data with only workContacts field

  Scenario: Successfully retrieve user data for given userId and institutionId with name, familyName, email, workContacts fields filter
    Given I have a valid userId and institutionId
    When I retrieve the user data with name, familyName, email, workContacts fields filter
    Then the response status should be 200
    And the response should contain the user data with only name, familyName, email, workContacts fields

  Scenario: Successfully retrieve user data for given fiscalCode and institutionId
    Given I have a valid fiscalCode and institutionId
    When I retrieve the user data
    Then the response status should be 200
    And the response should contain the user data

  Scenario: Retrieving user data for given fiscalCode without institutionId filter
    Given I have a valid fiscalCode
    When I retrieve the user data
    Then the response status should be 400
    And the response should contain the error message "institutionId is required"

  Scenario: Retrieving user data for given fiscalCode with institutionId filter, not found
    Given I have a valid fiscalCode and institutionId
    When I retrieve the user data
    Then the response status should be 404
    And the response should contain the error message "User not found"

  Scenario: Successfully update user email for given userId and institutionId
    Given I have a valid email, userId and institutionId
    When I update the user email
    Then the response status should be 200
    And the user email should be updated

  Scenario: Successfully update user mobilePhone for given userId and institutionId
    Given I have a valid mobilePhone, userId and institutionId
    When I update the user mobilePhone
    Then the response status should be 200
    And the user mobilePhone should be updated

  Scenario: Updating user email for given userId without institutionId
    Given I have a valid email and userId
    When I update the user email
    Then the response status should be 400
    And the response should contain the error message "institutionId is required"

  Scenario: Updating user email for given userId with non existent institutionId
    Given I have a valid email, userId and non existent institutionId
    When I update the user email
    Then the response status should be 404
    And the response should contain the error message "Institution not found"

  Scenario: Updating user invalid mobilePhone for given userId
    Given I have an invalid mobilePhone
    When I update the user mobilePhone
    Then the response status should be 400
    And the response should contain the error message "Invalid mobilePhone"

  Scenario: Updating user invalid email for given userId
    Given I have an invalid email
    When I update the user email
    Then the response status should be 400
    And the response should contain the error message "Invalid email"

  Scenario: Updating user name for given userId when name is certified fields
    Given I have a valid name, userId and institutionId
    When I update the user name
    Then the response status should be 400
    And the response should contain the error message "Name is certified"

  Scenario: Successfully retrieve users for given institutionId without filters
    Given I have a valid institutionId
    When I retrieve the users
    Then the response status should be 200
    And the response should contain the users

  Scenario: Successfully retrieve users for given institutionId with roles filter
    Given I have a valid institutionId and roles
    When I retrieve the users with roles filter
    Then the response status should be 200
    And the response should contain the users with roles filter

  Scenario: Successfully retrieve users for given institutionId with productRoles filter
    Given I have a valid institutionId and productRoles
    When I retrieve the users with productRoles filter
    Then the response status should be 200
    And the response should contain the users with productRoles filter

  Scenario: Successfully retrieve users for given institutionId with productId filter
    Given I have a valid institutionId and productId
    When I retrieve the users with productId filter
    Then the response status should be 200
    And the response should contain the users with productId filter

  Scenario: Successfully retrieve users for given institutionId with productId, roles and productRoles filters
    Given I have a valid institutionId, productId, roles and productRoles
    When I retrieve the users with productId, roles and productRoles filters
    Then the response status should be 200
    And the response should contain the users with productId, roles and productRoles filters

  Scenario: Successfully retrieve users for given institutionId with productId, roles and productRoles filters, no users found
    Given I have a valid institutionId, productId, roles and productRoles
    When I retrieve the users with productId, roles and productRoles filters
    Then the response status should be 200
    And the response should contain empty list

  Scenario: Successfully retrieve users for given institutionId, no users found
    Given I have a valid institutionId
    When I retrieve the users
    Then the response status should be 200
    And the response should contain empty list





