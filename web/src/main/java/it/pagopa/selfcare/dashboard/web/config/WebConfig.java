package it.pagopa.selfcare.dashboard.web.config;

import it.pagopa.selfcare.commons.web.config.BaseWebConfig;
import it.pagopa.selfcare.commons.web.handler.RestExceptionsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;

@Configuration
@PropertySource("classpath:config/web-config.properties")
@Import(RestExceptionsHandler.class)
class WebConfig extends BaseWebConfig {

    @Autowired
    WebConfig(Collection<HandlerInterceptor> interceptors) {
        super(interceptors);
    }

}
