package it.pagopa.selfcare.dashboard.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreDelegationApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreManagementApiRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {CoreManagementApiRestClient.class})
@PropertySource("classpath:config/ms-core-rest-client.properties")
public class CoreManagementApiRestClientConfig {
}
