package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import lombok.Data;

import java.util.List;

@Data
public class OnBoardingPnPGInfo {
    private String userId;
    private List<OnboardingPnPGData> institutions;
}
