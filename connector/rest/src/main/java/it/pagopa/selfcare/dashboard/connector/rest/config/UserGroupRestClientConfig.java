package it.pagopa.selfcare.dashboard.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserGroupRestClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.*;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = UserGroupRestClient.class)
@PropertySource("classpath:config/user-group-rest-client.properties")
class UserGroupRestClientConfig {

    @Bean
    @Primary
    public PageableSpringEncoder pageableSpringEncoder(SpringEncoder springEncoder) {
        return new PageableSpringEncoder(springEncoder);
    }

}
