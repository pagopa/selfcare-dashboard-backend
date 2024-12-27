package it.pagopa.selfcare.dashboard.integration_test.steps;

import it.pagopa.selfcare.dashboard.integration_test.model.Filter;
import it.pagopa.selfcare.dashboard.integration_test.model.Requests;
import it.pagopa.selfcare.dashboard.integration_test.model.Responses;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationRequestDto;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationType;
import it.pagopa.selfcare.dashboard.model.support.SupportRequestDto;
import it.pagopa.selfcare.dashboard.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.model.user_groups.UpdateUserGroupDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DashboardStepsUtil {

    protected String errorMessage;
    protected int status;
    protected Filter filter = new Filter();
    protected Responses responses = new Responses();
    protected Requests requests = new Requests();
    protected String token;

    public DelegationRequestDto toDelegationRequestDto(Map<String, String> entry) {
        DelegationRequestDto delegationRequestDto = new DelegationRequestDto();
        delegationRequestDto.setFrom(entry.get("from"));
        delegationRequestDto.setTo(entry.get("to"));
        delegationRequestDto.setProductId(entry.get("productId"));
        delegationRequestDto.setType(DelegationType.valueOf(entry.get("type")));
        delegationRequestDto.setInstitutionFromName(entry.get("institutionFromName"));
        delegationRequestDto.setInstitutionToName(entry.get("institutionToName"));
        return delegationRequestDto;
    }

    public CreateUserGroupDto toCreateUserGroupDto(Map<String, String> entry) {
        CreateUserGroupDto createUserGroupDtos = new CreateUserGroupDto();
        createUserGroupDtos.setInstitutionId(entry.get("institutionId"));
        createUserGroupDtos.setProductId(entry.get("productId"));
        createUserGroupDtos.setName(entry.get("name"));
        createUserGroupDtos.setDescription(entry.get("description"));
        createUserGroupDtos.setMembers(Optional.ofNullable(entry.get("members")).map(s -> Set.of(entry.get("members").split(",")).stream()
                .map(UUID::fromString).collect(Collectors.toSet())).orElse(null));
        return createUserGroupDtos;
    }

    public UpdateUserGroupDto toUpdateUserGroupDto(Map<String, String> entry) {
        UpdateUserGroupDto updateUserGroupDto = new UpdateUserGroupDto();
        updateUserGroupDto.setName(entry.get("name"));
        updateUserGroupDto.setDescription(entry.get("description"));
        updateUserGroupDto.setMembers(Optional.ofNullable(entry.get("members")).map(s -> Set.of(entry.get("members").split(",")).stream()
                .map(UUID::fromString).collect(Collectors.toSet())).orElse(null));
        return updateUserGroupDto;
    }

    public SupportRequestDto toSupportRequestDto(Map<String, String> entry) {
        SupportRequestDto supportRequestDto = new SupportRequestDto();
        supportRequestDto.setEmail(entry.get("email"));
        supportRequestDto.setUserId(entry.get("userId"));
        supportRequestDto.setInstitutionId(entry.get("institutionId"));
        supportRequestDto.setProductId(entry.get("productId"));
        return supportRequestDto;
    }

}
