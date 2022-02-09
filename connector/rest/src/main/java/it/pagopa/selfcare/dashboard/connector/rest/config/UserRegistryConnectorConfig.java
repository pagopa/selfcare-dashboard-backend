package it.pagopa.selfcare.dashboard.connector.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:config/user-registry-connector.properties")
public class UserRegistryConnectorConfig {
}
