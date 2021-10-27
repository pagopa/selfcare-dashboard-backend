package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.model.party_mgmt.Organization;
import org.springframework.web.bind.annotation.PathVariable;

public interface PartyManagementService {

    Organization getOrganization(String organizationId);
}
