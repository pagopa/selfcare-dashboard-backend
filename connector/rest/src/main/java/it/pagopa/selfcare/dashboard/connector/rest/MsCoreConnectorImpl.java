package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class MsCoreConnectorImpl implements MsCoreConnector {

    private final MsCoreRestClient msCoreRestClient;


    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient msCoreRestClient) {
        this.msCoreRestClient = msCoreRestClient;
    }


    @Override
    public InstitutionInfo getPGInstitutionByExternalId(String externalId) {
        log.trace("getPGInstitutionByExternalId start");
        log.debug("getPGInstitutionByExternalId institutionId = {}", externalId);
        InstitutionInfo result = msCoreRestClient.getPGInstitutionByExternalId(externalId);
        log.debug("getPGInstitutionByExternalId result = {}", result);
        log.trace("getPGInstitutionByExternalId end");
        return result;
    }

}
