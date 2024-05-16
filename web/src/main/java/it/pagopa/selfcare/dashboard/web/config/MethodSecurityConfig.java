package it.pagopa.selfcare.dashboard.web.config;

import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.web.security.SelfCarePermissionEvaluatorV2;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final UserApiConnector userApiConnector;

    private final UserGroupConnector userGroupConnector;


    public MethodSecurityConfig(UserApiConnector userApiConnector,
                                UserGroupConnector userGroupConnector) {
        this.userApiConnector = userApiConnector;
        this.userGroupConnector = userGroupConnector;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new SelfCarePermissionEvaluatorV2(userApiConnector, userGroupConnector));
        return expressionHandler;
    }

}
