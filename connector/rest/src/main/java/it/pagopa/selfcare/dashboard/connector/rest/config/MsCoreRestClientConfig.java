package it.pagopa.selfcare.dashboard.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreUserApiRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {MsCoreRestClient.class, MsCoreUserApiRestClient.class})
@PropertySource("classpath:config/ms-core-rest-client.properties")
class MsCoreRestClientConfig {
}
