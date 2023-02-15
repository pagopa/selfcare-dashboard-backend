package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.PnPGInstitutionLegalAddressData;

public interface PartyRegistryProxyConnector {

    PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId);

}
