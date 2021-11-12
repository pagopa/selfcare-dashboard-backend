package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import lombok.Data;

import java.util.List;

@Data
public class OnBoardingInfo {
    private PersonInfo person;
    private List<OnboardingData> institutions;
}
