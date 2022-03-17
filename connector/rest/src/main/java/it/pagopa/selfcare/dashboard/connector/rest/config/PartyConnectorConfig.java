package it.pagopa.selfcare.dashboard.connector.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/party-connector.properties")
class PartyConnectorConfig {
}
