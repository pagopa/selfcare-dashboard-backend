package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class UserRegistryServiceImpl implements UserRegistryService {

    private final UserRegistryConnector userConnector;

    @Autowired
    public UserRegistryServiceImpl(UserRegistryConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Override
    public User getUser(String externalId) {
        log.trace("getUser start");
        log.debug("getUser externalId = {}", externalId);
        Assert.hasText(externalId, "A TaxCode is required");
        User result = userConnector.getUser(externalId);
        log.debug("getUser result = {}", result);
        log.trace("getUser end");
        return result;
    }
}
