package it.pagopa.selfcare.dashboard.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.client.MsBackOfficeChannelApiClient;
import it.pagopa.selfcare.dashboard.client.MsBackOfficeStationApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = {MsBackOfficeStationApiClient.class, MsBackOfficeChannelApiClient.class})
@PropertySource("classpath:config/pago-pa-backoffice-rest-client.properties")
public class PagoPABackOfficeRestClientConfig {
}
