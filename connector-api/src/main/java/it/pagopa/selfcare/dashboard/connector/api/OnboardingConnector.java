package it.pagopa.selfcare.dashboard.connector.api;

public interface OnboardingConnector {

    Boolean getOnboardingWithFilter(String institutionId, String productId, String status);
}
