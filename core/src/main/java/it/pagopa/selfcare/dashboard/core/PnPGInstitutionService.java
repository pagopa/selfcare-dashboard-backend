package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;

import java.util.Collection;
import java.util.List;

public interface PnPGInstitutionService {

    List<PartyProduct> getInstitutionProducts(String institutionId);

}
