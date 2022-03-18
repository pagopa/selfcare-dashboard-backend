package it.pagopa.selfcare.dashboard.connector.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
<<<<<<<< HEAD:connector/rest/src/main/java/it/pagopa/selfcare/dashboard/connector/rest/config/FeignClientConfig.java
@PropertySource("classpath:config/feign-client.properties")
public class FeignClientConfig {
========
@PropertySource("classpath:config/party-connector.properties")
class PartyConnectorConfig {
>>>>>>>> release-dev:connector/rest/src/main/java/it/pagopa/selfcare/dashboard/connector/rest/config/PartyConnectorConfig.java
}
