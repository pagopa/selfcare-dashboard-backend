package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.PnPGInstitutionLegalAddressData;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyRegistryProxyRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
class PartyRegistryProxyConnectorImpl implements PartyRegistryProxyConnector {
    private static final String REQUIRED_EXTERNAL_ID_MESSAGE = "An institution's external id is required ";

    private final PartyRegistryProxyRestClient restClient;


    @Autowired
    public PartyRegistryProxyConnectorImpl(PartyRegistryProxyRestClient restClient) {
        this.restClient = restClient;
    }


    @Override
    public PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_EXTERNAL_ID_MESSAGE);
        PnPGInstitutionLegalAddressData result = restClient.getInstitutionLegalAddress(externalInstitutionId);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

}
