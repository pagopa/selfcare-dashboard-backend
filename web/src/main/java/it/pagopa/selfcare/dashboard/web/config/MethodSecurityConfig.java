package it.pagopa.selfcare.dashboard.web.config;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.web.security.SelfCarePermissionEvaluator;
import it.pagopa.selfcare.dashboard.web.security.SelfCarePermissionEvaluatorV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final MsCoreConnector msCoreConnector;
    private final UserApiConnector userApiConnector;
    private final String securityConnectorType;

    public MethodSecurityConfig(MsCoreConnector msCoreConnector,
                                UserApiConnector userApiConnector,
                                @Value("${dashboard.security.connector}") String securityConnectorType) {
        this.msCoreConnector = msCoreConnector;
        this.userApiConnector = userApiConnector;
        this.securityConnectorType = securityConnectorType;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        if(securityConnectorType.equalsIgnoreCase("v1")) {
            expressionHandler.setPermissionEvaluator(new SelfCarePermissionEvaluator(msCoreConnector));
        }else {
            expressionHandler.setPermissionEvaluator(new SelfCarePermissionEvaluatorV2(userApiConnector));
        }
        return expressionHandler;
    }

}
