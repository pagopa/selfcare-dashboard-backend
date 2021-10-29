package it.pagopa.selfcare.dashboard.core.party_mgmt;

import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;

public interface PartyManagementService {

    Organization getOrganization(String organizationId);
}
