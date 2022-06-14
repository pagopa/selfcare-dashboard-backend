package it.pagopa.selfcare.dashboard.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExchangeTokenProperties.class)
public class ExchangeTokenConfig {
}

