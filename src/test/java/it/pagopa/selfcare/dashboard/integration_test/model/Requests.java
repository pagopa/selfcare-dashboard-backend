package it.pagopa.selfcare.dashboard.integration_test.model;

import it.pagopa.selfcare.dashboard.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.model.support.SupportRequestDto;
import it.pagopa.selfcare.dashboard.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.model.user_groups.UpdateUserGroupDto;
import lombok.Data;

@Data
public class Requests {
    private SupportRequestDto supportRequestDto;
    private DelegationRequestDto delegationRequestDto;
    private CreateUserGroupDto createUserGroupDto;
    private UpdateUserGroupDto updateUserGroupDto;
}
