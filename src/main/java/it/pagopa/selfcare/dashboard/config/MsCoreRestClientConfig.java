package it.pagopa.selfcare.dashboard.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.client.CoreDelegationApiRestClient;
import it.pagopa.selfcare.dashboard.client.CoreInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.client.CoreManagementApiRestClient;
import it.pagopa.selfcare.dashboard.client.CoreOnboardingApiRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {CoreDelegationApiRestClient.class,
        CoreInstitutionApiRestClient.class, CoreOnboardingApiRestClient.class, CoreManagementApiRestClient.class})
@PropertySource("classpath:config/ms-core-rest-client.properties")
public class MsCoreRestClientConfig {
}
