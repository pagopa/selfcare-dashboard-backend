package it.pagopa.selfcare.dashboard.connector.onboarding;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingRequestInfo {

    private InstitutionInfo institutionInfo;
    private UserInfo manager;
    private List<UserInfo> admins;

}
