@exclude
Feature: UserGroups
@exclude
  Scenario: Successfully create a new user group
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description | productId  | institutionId                        | members                                                                   |
      | Group Name | TestGroup   | prod-io    | c9a50656-f345-4c81-84be-5b2474470544 | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 201
    And the response should contain a valid user group id
@exclude
  Scenario: Attempt to create a group without permissions
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description | productId       | institutionId                        | members                                                                   |
      | Group Name | TestGroup   | prod-io-premium | c9a50656-f345-4c81-84be-5b2474470544 | 525db33f-967f-4a82-8984-c606225e714a,a1b7c86b-d195-41d8-8291-7c3467abfd30 |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 404
@exclude
  Scenario: Attempt to create a group with invalid members:
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description | productId  | institutionId | members                             |
      | Group Name | TestGroup   | prod-io    | c9a50656-f345-4c81-84be-5b2474470544       | 97a511a7-2acc-47b9-afed-2f3c65753ccc|
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 500
    And the response should contain an error message "Some members in the list aren't allowed for this institution"
@exclude
  Scenario: Attempt to create a user group with a duplicate name
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description | productId  | institutionId                        | members                                                                   |
      | Group Name | TestGroup   | prod-io    | c9a50656-f345-4c81-84be-5b2474470544 | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 409
@exclude
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | description | productId  | institutionId                        | members                                                                   |
      | TestGroup   | prod-io    | c9a50656-f345-4c81-84be-5b2474470544 | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.name,must not be blank"

@exclude
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description | productId   | members                                                                   |
      | Group Name | TestGroup   | prod-io     | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.institutionId,must not be blank"

@exclude
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description  | institutionId                        | members                                                                   |
      | Group Name | TestGroup    | c9a50656-f345-4c81-84be-5b2474470544 | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.productId,must not be blank"

@exclude
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name        | productId  | institutionId                        | members                                                                   |
      | Group Name  | prod-io    | c9a50656-f345-4c81-84be-5b2474470544 | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.description,must not be blank"

@exclude
  Scenario: Attempt to create a user group with missing required fields
    Given user login with username "j.doe" and password "test"
    And the following user group details:
      | name       | description | productId  | institutionId                        |
      | Group Name | TestGroup   | prod-io    | c9a50656-f345-4c81-84be-5b2474470544 |
    When I send a POST request to "/v2/user-groups/" with the given details to create usergroup
    Then the response status should be 400
    And the response should contain an error message "createUserGroupDto.members,must not be empty"
@exclude
  Scenario: Successfully update a group with a valid ID and valid data
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have data to update:
      | name         | description        | members                                                                   |
      | updated Name | updatedDescription | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753b4a |
    When I send a PUT request to "/v2/user-groups/{groupId}" to update userGroup
    Then the response status should be 204
@exclude
  Scenario: Attempt to update a group with invalid members:
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have data to update:
      | name         | description        | members                                                                   |
      | updated Name | updatedDescription | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753ccc |
    When I send a PUT request to "/v2/user-groups/{groupId}" to update userGroup
    Then the response status should be 500
    And the response should contain an error message "Some members in the list aren't allowed for this institution"
@exclude
  Scenario: Attempt to update a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "non-existent-group-id"
    And I have data to update:
      | name         | description        | members                                                                   |
      | updated Name | updatedDescription | 35a78332-d038-4bfa-8e85-2cba7f6b7bf7,97a511a7-2acc-47b9-afed-2f3c65753ccc |
    When I send a PUT request to "/v2/user-groups/{groupId}" to update userGroup
    Then the response status should be 404
  @exclude
  Scenario: Successfully suspend a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a POST request to "/v2/user-groups/{groupId}/suspend" to update userGroup status
    Then the response status should be 204
  @exclude
  Scenario: Attempt to update a suspended group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have data to update:
      | name       | description | members                                                                   |
      | Group Name | TestGroup   | 97a511a7-2acc-47b9-afed-2f3c65753b4a|
    When I send a PUT request to "/v2/user-groups/{groupId}" to update userGroup
    Then the response status should be 400
  @exclude
  Scenario: Attempt to suspend a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a POST request to "/v2/user-groups/{groupId}/suspend" to update userGroup status
    Then the response status should be 404
  @exclude
  Scenario: Successfully activate a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a POST request to "/v2/user-groups/{groupId}/activate" to update userGroup status
    Then the response status should be 204
    And the retrieved group should be changed status to "ACTIVE"
  @exclude
  Scenario: Attempt to activate a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a POST request to "/v2/user-groups/{groupId}/activate" to update userGroup status
    Then the response status should be 404
  @exclude
  Scenario: Successfully delete a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a DELETE request to "/v2/user-groups/{groupId}" to delete userGroup
    Then the response status should be 204
    And the retrieved group should be changed status to "DELETED"
  @exclude
  Scenario: Attempt to delete a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a DELETE request to "/v2/user-groups/{groupId}" to delete userGroup
    Then the response status should be 404

  Scenario: Successfully retrieve a group with a valid ID without institutionId filter
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    When I send a GET request to "/v2/user-groups/{id}" to retrieve userGroup
    Then the response status should be 200
    And the response should contain the group details
  @exclude
  Scenario: Attempt to retrieve a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "non-existent-group-id"
    When I send a GET request to "/v2/user-groups/{id}" to retrieve userGroup
    Then the response status should be 404
    And the response should contain an error message "Not Found"
  @exclude
  Scenario: Attempt to retrieve a group with a valid ID with valid institutionId filter
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And the institutionId is "c9a50656-f345-4c81-84be-5b2474470544"
    When I send a GET request to "/v2/user-groups/{id}" to retrieve userGroup
    Then the response status should be 200
    And the response should contain the group details
  @exclude
  Scenario: Attempt to retrieve a group with a valid ID with invalid institutionId filter
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And the institutionId is "467ac77d-7faa-47bf-a60e-38ea74bd5fd2"
    When I send a GET request to "/v2/user-groups/{id}" to retrieve userGroup
    Then the response status should be 404
  @exclude
  Scenario: Successfully retrieve user groups with valid filters
    Given user login with username "j.doe" and password "test"
    And the institutionId is "9c8ae123-d990-4400-b043-67a60aff31bc"
    And the productId is "prod-test"
    And I have memberId "uuid"
    When I send a GET request to "/v2/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain 1 item
    And the response should contain the group details
  @exclude
  Scenario: Successfully retrieve user groups without any filters
    Given user login with username "j.doe" and password "test"
    When I send a GET request to "/v2/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain 3 item
    And the response should contains groupIds "6759f8df78b6af202b222d29,6759f8df78b6af202b222d2a,6759f8df78b6af202b222d2b"
  @exclude
  Scenario: Attempt to retrieve user groups with a sorting parameter but no productId or institutionId
    Given user login with username "j.doe" and password "test"
    Given I have a filter with sorting by "name"
    When I send a GET request to "/v2/user-groups" to retrieve userGroups
    Then the response status should be 400
    And the response should contain an error message "Given sort parameters aren't valid"
  @exclude
  Scenario: Successfully retrieve a paginated list of user groups
    Given user login with username "j.doe" and password "test"
    And I set the page number to 0 and page size to 2
    When I send a GET request to "/v2/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain a paginated list of user groups of 2 items on page 0
  @exclude
  Scenario: Successfully retrieve a paginated list of user groups
    Given user login with username "j.doe" and password "test"
    And I set the page number to 1 and page size to 2
    When I send a GET request to "/v2/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain a paginated list of user groups of 1 items on page 1
  @exclude
  Scenario: No user groups found for the provided filters
    Given user login with username "j.doe" and password "test"
    And the institutionId is "9c8ae123-d990-4400-b043-67a60affabcd"
    And the productId is "prod-test"
    And I have memberId "uuid"
    When I send a GET request to "/v2/user-groups" to retrieve userGroups
    Then the response status should be 200
    And the response should contain an empty list
  @exclude
  Scenario: Successfully add a member to a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v2/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 204
  @exclude
  Scenario: Attempt to add member to a group without permissions
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v2/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 401
  @exclude
  Scenario: Attempt to add a member to a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "9999"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v2/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 404
    And the response should contain an error message "Not Found"
  @exclude
  Scenario: Attempt to add a member to a suspended group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d2b"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a POST request to "/v2/user-groups/{id}/members/{memberId}" to add userGroup member
    Then the response status should be 400
    And the response should contain an error message "Trying to modify suspended group"
  @exclude
  Scenario: Successfully delete a member from a group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v2/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 204
  @exclude
  Scenario: Attempt to delete a group without permissions
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v2/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 401
  @exclude
  Scenario: Attempt to delete a member from a non-existent group
    Given user login with username "j.doe" and password "test"
    And I have groupId "99999"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v2/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 404
    And the response should contain an error message "Not Found"
  @exclude
  Scenario: Attempt to delete a member from a suspended group
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d2b"
    And I have memberId "78bb7f07-0464-4ff9-a0ee-82568902b7bf"
    When I send a DELETE request to "/v2/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 400
    And the response should contain an error message "Trying to modify suspended group"

    @exclude
  Scenario: Attempt to delete a member with non-existent member ID
    Given user login with username "j.doe" and password "test"
    And I have groupId "6759f8df78b6af202b222d29"
    And I have memberId "f71dcad0-3374-4b51-91b8-75a9b36a5696"
    When I send a DELETE request to "/v2/user-groups/{id}/members/{memberId}" to delete member
    Then the response status should be 204
