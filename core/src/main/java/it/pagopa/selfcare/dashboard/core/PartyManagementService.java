package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;

public interface PartyManagementService {

    Organization getOrganization(String organizationId);
}
