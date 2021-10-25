package it.pagopa.selfcare.dashboard.connector.rest.config;

import it.pagopa.selfcare.dashboard.connector.rest.PartyRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = PartyRestClient.class)
@PropertySource("classpath:config/party-rest-client.properties")
public class PartyRestClientConfig {
}
