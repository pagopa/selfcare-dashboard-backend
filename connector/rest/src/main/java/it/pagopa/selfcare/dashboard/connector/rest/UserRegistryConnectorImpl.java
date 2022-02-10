package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.TargetEnvironment;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.Function;

@Slf4j
@Service
public class UserRegistryConnectorImpl implements UserRegistryConnector {

    private static final Function<UserResponse, User> USER_RESPONSE_TO_USER_FUNCTION = userResponse -> {
        User user = new User();
        if (userResponse != null) {
            user.setName(userResponse.getName());
            user.setSurname(userResponse.getSurname());
            user.setFiscalCode(userResponse.getExternalId());
            if (userResponse.getCertification() != null && !Certification.NONE.equals(userResponse.getCertification())) {
                user.setCertification(true);
            }
            if (userResponse.getExtras() != null) {
                user.setEmail(userResponse.getExtras().getEmail());
            }
        }
        return user;
    };
    private final UserRegistryRestClient restClient;

    @Autowired
    public UserRegistryConnectorImpl(UserRegistryRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public User getUser(String externalId) {
        log.trace("getUser start");
        if (!TargetEnvironment.PROD.equals(TargetEnvironment.getCurrent())) {
            log.debug("getUser externalId = {}", externalId);
        }
        Assert.hasText(externalId, "A TaxCode is required");

        UserResponse userResponse = restClient.getUserByExternalId(new EmbeddedExternalId(externalId));
        User result = USER_RESPONSE_TO_USER_FUNCTION.apply(userResponse);
        log.debug("getUser result = {}", result);
        log.trace("getUser start");

        return result;
    }
}
