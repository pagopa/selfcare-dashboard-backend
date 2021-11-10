package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyManagementConnector;
import it.pagopa.selfcare.dashboard.connector.model.organization.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PartyManagementServiceImpl implements PartyManagementService {

    private final PartyManagementConnector partyManagementConnector;

    @Autowired
    public PartyManagementServiceImpl(PartyManagementConnector partyManagementConnector) {
        this.partyManagementConnector = partyManagementConnector;
    }

    @Override
    public Organization getOrganization(String organizationId) {
        return partyManagementConnector.getOrganization(organizationId);
    }
}
