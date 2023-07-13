package it.pagopa.selfcare.dashboard.connector.rest.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BackOfficeAuthorizationInterceptor implements RequestInterceptor {
    @Value("${authorization.pagopa.subscriptionKey}")
    private String pagoPApiSubscriptionKey;

    @Override
    public void apply(RequestTemplate template) {
        template.header("Ocp-Apim-Subscription-Key", pagoPApiSubscriptionKey);
    }
}
