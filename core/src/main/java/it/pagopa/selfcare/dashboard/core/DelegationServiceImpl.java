package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
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
    public List<Delegation> getDelegations(String from, String to, String productId, String search, String taxCode, String mode, String order, Integer page, Integer size) {
        log.trace("getDelegations start");
        log.debug("getDelegations from = {}, to = {}, productId = {}", from, to, productId);
        List<Delegation> result = msCoreConnector.getDelegations(from, to, productId, search, taxCode, order, mode, page, size);
        log.debug("getDelegations result = {}", result);
        log.trace("getDelegations end");
        return result;
    }


}