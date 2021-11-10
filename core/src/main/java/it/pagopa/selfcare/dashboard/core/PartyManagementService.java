package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.organization.Organization;

public interface PartyManagementService {

    Organization getOrganization(String organizationId);
}
