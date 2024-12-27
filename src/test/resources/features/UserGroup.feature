@exclude
Feature: UserGroups

  Scenario: Successfully create a new user group
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | productId  | institutionId | status | members                                                                   |
      | CREATE_USERGROUP | Group Name | TestGroup   | product123 | inst123       | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 201
    And the response should contain a valid user group id

  Scenario: Attempt to create a group without permissions
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | productId  | institutionId | status | members                                                                   |
      | CREATE_USERGROUP | Group Name | TestGroup   | product123 | inst123       | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 401

  Scenario: Attempt to create a group with invalid members:
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | productId  | institutionId | status | members                                                                   |
      | CREATE_USERGROUP | Group Name | TestGroup   | product123 | inst123       | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "Some members in the list aren't allowed for this institution"

    # Scenario negativo: Nome del gruppo già esistente (conflitto)
  Scenario: Attempt to create a user group with a duplicate name
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name     | description | productId | institutionId                        | status | members                                                                   |
      | CREATE_USERGROUP | io group | TestGroup   | prod-test | 9c8ae123-d990-4400-b043-67a60aff31bc | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 409
    And the response should contain an error message "A group with the same name already exists in ACTIVE or SUSPENDED state"

  # Scenario negativo: Dettagli del gruppo mancanti (name non fornito)
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | description | productId  | institutionId | status | members                                                                   |
      | CREATE_USERGROUP | TestGroup   | product123 | inst123       | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.name,must not be blank"

  # Scenario negativo: Dettagli del gruppo mancanti (institutionId non fornito)
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | productId  | status | members                                                                   |
      | CREATE_USERGROUP | Group Name | TestGroup   | product123 | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.institutionId,must not be blank"

  # Scenario negativo: Dettagli del gruppo mancanti (productId non fornito)
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | institutionId | status | members                                                                   |
      | CREATE_USERGROUP | Group Name | TestGroup   | inst123       | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.productId,must not be blank"

  # Scenario negativo: Dettagli del gruppo mancanti (description non fornito)
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | productId  | institutionId | status | members                                                                   |
      | CREATE_USERGROUP | Group Name | product123 | inst123       | ACTIVE | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.description,must not be blank"

  # Scenario negativo: Dettagli del gruppo mancanti (status non fornito)
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | productId  | institutionId | members                                                                   |
      | CREATE_USERGROUP | Group Name | TestGroup   | product123 | inst123       | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.status,must not be null"

  # Scenario negativo: Dettagli del gruppo mancanti (members vuoto)
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "r.balboa" and password "test"
    And the following user group details:
      | Api              | name       | description | productId  | institutionId | status | members |
      | CREATE_USERGROUP | Group Name | TestGroup   | product123 | inst123       | ACTIVE |         |
    When I send a POST request to "/v2/user-groups" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.members,must not be empty"

  Scenario: Successfully update a group with a valid ID and valid data
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have data to update:
      | Api              | name         | description        | members                                                                   |
      | UPDATE_USERGROUP | updated Name | updatedDescription | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a PUT request to "/v1/user-groups/{groupId}" to update userGroup
    Then the response status should be 200
    And the retrieved group should be updated

  Scenario: Attempt to update a group with invalid members:
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have data to update:
      | Api              | name         | description        | members                               |
      | UPDATE_USERGROUP | updated Name | updatedDescription | 525db33f-967f-4a82-8984-c606225e7452, |
    When I send a PUT request to "/v1/user-groups/{groupId}" to update userGroup
    Then the response status should be 400
    And the response should contain an error message "Some members in the list aren't allowed for this institution"

  Scenario: Attempt to update a non-existent group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "non-existent-group-id"
    And I have data to update:
      | Api              | name       | description | members                                                                   |
      | UPDATE_USERGROUP | Group Name | TestGroup   | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a PUT request to "/v1/user-groups/{groupId}" to update userGroup
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Attempt to update a suspended group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have data to update:
      | Api              | name       | description | members                                                                   |
      | UPDATE_USERGROUP | Group Name | TestGroup   | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a PUT request to "/v1/user-groups/{groupId}" to update userGroup
    Then the response status should be 400
    And the response should contain an error message "Trying to modify suspended group"

  Scenario: Successfully suspend a group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a POST request to "/v1/user-groups/{groupId}/suspend" to update userGroup status
    Then the response status should be 204
    And the retrieved group should be changed status to "SUSPENDED"

  Scenario: Attempt to suspend a group without permissions
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a POST request to "/v1/user-groups/{groupId}/suspend" to update userGroup status
    Then the response status should be 401

  Scenario: Attempt to suspend a non-existent group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a POST request to "/v1/user-groups/{groupId}/suspend" to update userGroup status
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Successfully activate a group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a POST request to "/v1/user-groups/{groupId}/activate" to update userGroup status
    Then the response status should be 204
    And the retrieved group should be changed status to "ACTIVE"

  Scenario: Attempt to activate a non-existent group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a POST request to "/v1/user-groups/{groupId}/activate" to update userGroup status
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Attempt to activate a group without permissions
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a POST request to "/v1/user-groups/{groupId}/activate" to update userGroup status
    Then the response status should be 401

  Scenario: Successfully delete a group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a DELETE request to "/v1/user-groups/{groupId}" to delete userGroup
    Then the response status should be 204
    And the retrieved group should be changed status to "DELETED"

  Scenario: Attempt to delete member from group without permissions
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a DELETE request to "/v1/user-groups/{groupId}" to delete userGroup
    Then the response status should be 401

  Scenario: Attempt to delete a non-existent group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a DELETE request to "/v1/user-groups/{groupId}" to delete userGroup
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Successfully retrieve a group with a valid ID without institutionId filter
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a GET request to "/v1/user-groups/{id}" to retrieve userGroup
    Then the response status should be 200
    And the response should contain the group details

  Scenario: Attempt to retrieve a group without permissions
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a GET request to "/v1/user-groups/{id}" to retrieve userGroup
    Then the response status should be 401

  Scenario: Attempt to retrieve a non-existent group
    Given user login with username "r.balboa" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a GET request to "/v1/user-groups/{id}" to retrieve userGroup
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Attempt to retrieve a group with a valid ID with valid institutionId filter
    Given user login with username "r.balboa" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a GET request to "/v1/user-groups/{id}" to retrieve userGroup
    Then the response status should be 200
    And the response should contain the group details

  Scenario: Attempt to retrieve a group with a valid ID with invalid institutionId filter
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a GET request to "/v1/user-groups/{id}" to retrieve userGroup
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Successfully retrieve user groups with valid filters
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "9c8ae123-d990-4400-b043-67a60aff31bc"
    And the productId is "prod-test"
    And I have memberId "uuid"
    When I send a GET request to "/v1/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain 1 item
    And the response should contain the group details

  Scenario: Successfully retrieve user groups without any filters
    Given user login with username "r.balboa" and password "test"
    When I send a GET request to "/v1/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain 3 item
    And the response should contains groupIds "6759f8df78b6af202b222d29,6759f8df78b6af202b222d2a,6759f8df78b6af202b222d2b"

  Scenario: Attempt to retrieve user groups with a sorting parameter but no productId or institutionId
    Given user login with username "r.balboa" and password "test"
    Given I have a filter with sorting by "name"
    When I send a GET request to "/v1/user-groups" to retrieve userGroups
    Then the response status should be 400
    And the response should contain an error message "Given sort parameters aren't valid"

  Scenario: Successfully retrieve a paginated list of user groups
    Given user login with username "r.balboa" and password "test"
    And I set the page number to 0 and page size to 2
    When I send a GET request to "/v1/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain a paginated list of user groups of 2 items on page 0

  Scenario: Successfully retrieve a paginated list of user groups
    Given user login with username "r.balboa" and password "test"
    And I set the page number to 1 and page size to 2
    When I send a GET request to "/v1/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain a paginated list of user groups of 1 items on page 1

  Scenario: No user groups found for the provided filters
    Given user login with username "r.balboa" and password "test"
    And the institutionId is "9c8ae123-d990-4400-b043-67a60affabcd"
    And the productId is "prod-test"
    And I have memberId "uuid"
    When I send a GET request to "/v1/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain an empty list

  Scenario: Successfully add a member to a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v1/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 204

  Scenario: Attempt to add member to a group without permissions
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v1/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 401

  Scenario: Attempt to add a member to a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "9999"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v1/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Attempt to add a member to a suspended group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d2b"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v1/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 400
    And the response should contain an error message "Trying to modify suspended group"

  Scenario: Successfully delete a member from a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v1/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 204

  Scenario: Attempt to delete a group without permissions
    Given user login with username "r.balboa" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v1/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 401

  Scenario: Attempt to delete a member from a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "99999"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v1/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 404
    And the response should contain an error message "Not Found"

  Scenario: Attempt to delete a member from a suspended group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d2b"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v1/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 400
    And the response should contain an error message "Trying to modify suspended group"

  Scenario: Attempt to delete a member with non-existent member ID
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "f71dcad0-3374-4b51-91b8-75a9b36a5696"
    When I send a DELETE request to "/v1/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 204
