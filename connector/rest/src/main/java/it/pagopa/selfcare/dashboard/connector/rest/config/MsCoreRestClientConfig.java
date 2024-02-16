package it.pagopa.selfcare.dashboard.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.connector.rest.client.*;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {CoreUserApiRestClient.class, CoreDelegationApiRestClient.class,
        CoreInstitutionApiRestClient.class, CoreOnboardingApiRestClient.class, CoreManagementApiRestClient.class})
@PropertySource("classpath:config/ms-core-rest-client.properties")
public class MsCoreRestClientConfig {
}
