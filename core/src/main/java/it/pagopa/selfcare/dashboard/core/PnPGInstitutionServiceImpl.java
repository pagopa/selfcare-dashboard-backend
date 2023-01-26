package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PnPGInstitutionServiceImpl implements PnPGInstitutionService {

    private final MsCoreConnector msCoreConnector;


    @Autowired
    public PnPGInstitutionServiceImpl(MsCoreConnector msCoreConnector) {
        this.msCoreConnector = msCoreConnector;
    }


    @Override
    public InstitutionInfo getPGInstitutionByExternalId(String externalId) {
        log.trace("getPGInstitutionByExternalId start");
        log.debug("getPGInstitutionByExternalId externalId = {}", externalId);
        InstitutionInfo result = msCoreConnector.getPGInstitutionByExternalId(externalId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getPGInstitutionByExternalId end");
        return result;
    }

}
