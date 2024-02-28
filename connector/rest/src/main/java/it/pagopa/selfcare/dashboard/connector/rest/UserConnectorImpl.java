package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.OnboardedProductState;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.SearchUserDto;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserProductsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConnectorImpl implements UserApiConnector {


    private final UserApiRestClient userApiRestClient;
    private final InstitutionMapper institutionMapper;

    private final UserMapper userMapper;

    @Override
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

    @Override
    public User getUserById(String userId) {
        log.trace("getUserById start");
        log.debug("getUserById id = {}", userId);
        User user = userMapper.toUser(userApiRestClient._usersIdDetailsGet(userId).getBody());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserById = {}", user);
        log.trace("getUserById end");
        return user;
    }

    @Override
    public User searchByFiscalCode(String fiscalCode) {
        log.trace("searchByFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode fiscalCode = {}", fiscalCode);
        User user = userMapper.toUser(userApiRestClient._usersSearchPost(SearchUserDto.builder().fiscalCode(fiscalCode).build()).getBody());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode user = {}", user);
        log.trace("searchByFiscalCode end");
        return user;
    }

    @Override
    public void suspendUserProduct(String userId, String institutionId, String productId) {
        log.trace("suspend start");
        log.debug("suspend userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.SUSPENDED);
        log.trace("suspend end");
    }

    @Override
    public void activateUserProduct(String userId, String institutionId, String productId) {
        log.trace("activate start");
        log.debug("activate userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.ACTIVE);
        log.trace("activate end");
    }

    @Override
    public void deleteUserProduct(String userId, String institutionId, String productId) {
        log.trace("delete start");
        log.debug("delete userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.DELETED);
        log.trace("delete end");
    }
}
