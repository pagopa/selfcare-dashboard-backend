package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserRequestDto;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
            user.setCertification(Certification.isCertified(userResponse.getCertification()));
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
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUser externalId = {}", externalId);

        Assert.hasText(externalId, "A TaxCode is required");

        UserResponse userResponse = restClient.getUserByExternalId(new EmbeddedExternalId(externalId));
        User result = USER_RESPONSE_TO_USER_FUNCTION.apply(userResponse);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUser result = {}", result);
        log.trace("getUser end");

        return result;
    }

    @Override
    public void saveUser(UUID id, String institutionId, UserDto userDto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser id = {}, institutionId = {}, userDto = {}}", id, institutionId, userDto);
        Assert.notNull(id, "A UUID is required");
        Assert.hasText(institutionId, "An institutionId is required");
        Map<String, Object> cFields = new HashMap<>();
        String institutionContactsKey = String.format("institutionContacts.%s.email", institutionId);
        cFields.put(institutionContactsKey, userDto.getEmail());
        cFields.put("name", userDto.getName());
        cFields.put("surname", userDto.getSurname());
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setCFields(cFields);
        restClient.patchUser(id, requestDto);
        log.trace("saveUser end");
    }
}
