package it.pagopa.selfcare.dashboard.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.client.OnboardingRestClient;
import it.pagopa.selfcare.dashboard.client.TokenRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {OnboardingRestClient.class, TokenRestClient.class})
@PropertySource("classpath:config/onboarding-rest-client.properties")
public class OnboardingRestClientConfig {
}
