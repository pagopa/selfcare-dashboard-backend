package it.pagopa.selfcare.dashboard.connector.api;

public interface OnboardingConnector {

    Boolean getOnboardingWithFilter(String taxCode, String subunitCode, String productId, String status);
}
