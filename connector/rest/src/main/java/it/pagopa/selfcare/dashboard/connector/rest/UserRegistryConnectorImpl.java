package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.CertifiableField;
import it.pagopa.selfcare.dashboard.connector.model.user.CertifiableFieldResource;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserRegistryRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.EmbeddedExternalId;
import it.pagopa.selfcare.dashboard.connector.rest.model.user_registry.UserResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class UserRegistryConnectorImpl implements UserRegistryConnector {

    private static final Function<UserResource, User> USER_RESPONSE_TO_USER_FUNCTION = userResource -> {
        User user = null;
        if (userResource != null) {
            user = new User();
            user.setId(userResource.getId().toString());
            user.setFiscalCode(userResource.getFiscalCode());
            user.setEmail(map(userResource.getEmail()));
            user.setName(map(userResource.getEmail()));
            user.setFamilyName(map(userResource.getEmail()));
//            user.setWorkContact(map(userResource.getWorkContacts()));
        }
        return user;
    };

    private static <T> CertifiableField<T> map(CertifiableFieldResource<T> certifiableFieldResource) {
        CertifiableField<T> certifiableField = null;
        if (certifiableFieldResource != null) {
            certifiableField = new CertifiableField<>();
            certifiableField.setValue(certifiableFieldResource.getValue());
            certifiableField.setCertification(certifiableFieldResource.getCertification());
        }
        return certifiableField;
    }

    private final UserRegistryRestClient restClient;

    @Autowired
    public UserRegistryConnectorImpl(UserRegistryRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public User search(String externalId) {
        log.trace("getUserByExternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId externalId = {}", externalId);
        Assert.hasText(externalId, "A TaxCode is required");

        UserResource userResource = restClient.getUserByExternalId(EnumSet.allOf(UserResource.Fields.class), new EmbeddedExternalId(externalId));
        User result = USER_RESPONSE_TO_USER_FUNCTION.apply(userResource);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByExternalId result = {}", result);
        log.trace("getUserByExternalId end");

        return result;
    }

    //TODO fix signature
    @Override
    public User getUserByInternalId(String userId) {
        log.trace("getUserByInternalId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId userId = {}", userId);

        Assert.hasText(userId, "A userId is required");
        //TODO fix passed fields
        UserResource userResource = restClient.getUserByInternalId(UUID.fromString(userId), EnumSet.of(UserResource.Fields.fiscalCode));
        User result = USER_RESPONSE_TO_USER_FUNCTION.apply(userResource);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserByInternalId result = {}", result);
        log.trace("getUserByInternalId end");
        return result;
    }

    @Override
    public void updateUser(UUID id, String institutionId, MutableUserFieldsDto userDto) {
        log.trace("saveUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "saveUser id = {}, institutionId = {}, userDto = {}}", id, institutionId, userDto);
        Assert.notNull(id, "A UUID is required");
        Assert.hasText(institutionId, "An institutionId is required");
        restClient.patchUser(id, userDto);
        log.trace("saveUser end");
    }
}
