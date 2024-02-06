package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
@ConditionalOnProperty(value = "dashboard.user.client.api-version", havingValue = "v2")
@RequiredArgsConstructor
public class UserConnectorImpl implements UserApiConnector {


    private final UserApiRestClient userApiRestClient;
    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;


    @Override
    public void updateUser(String userId, String institutionId, MutableUserFieldsDto userDto) {
        log.trace("updateUser start");
        log.debug("updateUser userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdUserRegistryPut(userId, institutionId, userMapper.toMutableUserFieldsDto(userDto));
        log.trace("updateUser end");
    }

    @Override
    @ConditionalOnProperty(value = "dashboard.user.client.api-version", havingValue = "v2")
    public List<InstitutionInfo> getUserProducts(String userId) {
        log.trace("getUserProducts start");
        UserProductsResponse productsInfoUsingGET = userApiRestClient._usersUserIdProductsGet(userId, null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name())).getBody();

        if(Objects.isNull(productsInfoUsingGET) ||
                Objects.isNull(productsInfoUsingGET.getBindings())) return List.of();

        List<InstitutionInfo> result = productsInfoUsingGET.getBindings().stream()
                .map(institutionMapper::toInstitutionInfo)
                .toList();
        log.debug("getUserProducts result = {}", result);
        log.trace("getUserProducts end");
        return result;
    }
}
