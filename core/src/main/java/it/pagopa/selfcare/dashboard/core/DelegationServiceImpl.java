package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
class DelegationServiceImpl implements DelegationService {

    private final MsCoreConnector msCoreConnector;


    @Autowired
    public DelegationServiceImpl(MsCoreConnector msCoreConnector) {
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public DelegationId createDelegation(DelegationRequest delegation) {
        log.trace("createDelegation start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createDelegation request = {}", delegation);
        DelegationId result = msCoreConnector.createDelegation(delegation);
        log.debug("createDelegation result = {}", result);
        log.trace("createDelegation end");
        return result;
    }

    @Override
    public List<Delegation> getDelegations(GetDelegationParameters delegationParameters) {
        log.trace("getDelegations start");
        List<Delegation> result = msCoreConnector.getDelegations(delegationParameters);
        log.debug("getDelegations result = {}", result);
        log.trace("getDelegations end");
        return result;
    }

    @Override
    public DelegationWithPagination getDelegationsV2(GetDelegationParameters delegationParameters) {
        log.trace("getDelegationsV2 start");
        DelegationWithPagination result = msCoreConnector.getDelegationsV2(delegationParameters);
        log.debug("getDelegationsV2 result = {}", result);
        log.trace("getDelegationsV2 end");
        return result;
    }


}