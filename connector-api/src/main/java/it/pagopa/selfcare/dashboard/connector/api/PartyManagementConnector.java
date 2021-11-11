package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.organization.Organization;

public interface PartyManagementConnector {

    Organization getOrganization(String organizationId);

}
