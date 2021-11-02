package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.client.PartyManagementRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;
import it.pagopa.selfcare.dashboard.core.PartyManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PartyManagementServiceImpl implements PartyManagementService {

    private final PartyManagementRestClient restClient;

    @Autowired
    public PartyManagementServiceImpl(PartyManagementRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Organization getOrganization(String organizationId) {
        return restClient.getOrganization(organizationId);
    }
}
