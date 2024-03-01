package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserV2ServiceImpl implements UserV2Service {

    private final MsCoreConnector msCoreConnector;
    private final UserApiConnector userApiConnector;
    private final UserGroupService userGroupService;

    @Override
    public Collection<InstitutionInfo> getInstitutions(String userId) {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = userApiConnector.getUserProducts(userId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public void deleteUserProduct(String userId, String institutionId, String productId) {
        log.trace("delete start");
        log.debug("delete userId = {} for institutionId = {} and product = {}", userId, institutionId, productId);
        userApiConnector.deleteUserProduct(userId, institutionId, productId);
        userGroupService.deleteMembersByUserId(userId, institutionId, productId);
        log.trace("delete end");
    }

    @Override
    public void activateUserProduct(String userId, String institutionId, String productId) {
        log.trace("activate start");
        log.debug("activate userId = {} for institutionId = {} and product = {}", userId, institutionId, productId);
        userApiConnector.activateUserProduct(userId, institutionId, productId);
        log.trace("activate end");

    }

    @Override
    public void suspendUserProduct(String userId, String institutionId, String productId) {
        log.trace("suspend start");
        log.debug("suspend userId = {} for institutionId = {} and product = {}", userId, institutionId, productId);
        userApiConnector.suspendUserProduct(userId, institutionId, productId);
        log.trace("suspend end");
    }

    @Override
    public User getUserById(String userId) {
        log.trace("getUserById start");
        log.debug("getUserById id = {}", userId);
        User user = userApiConnector.getUserById(userId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserById = {}", user);
        log.trace("getUserById end");
        return user;
    }

    @Override
    public User searchUserByFiscalCode(String fiscalCode) {
        log.trace("searchByFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode fiscalCode = {}", fiscalCode);
        User user = userApiConnector.searchByFiscalCode(fiscalCode);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode user = {}", user);
        log.trace("searchByFiscalCode end");
        return user;
    }

}
