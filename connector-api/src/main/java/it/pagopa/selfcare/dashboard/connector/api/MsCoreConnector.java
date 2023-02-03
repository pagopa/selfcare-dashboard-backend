package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;

import java.util.Collection;
import java.util.List;

public interface MsCoreConnector {

    Collection<InstitutionInfo> getOnBoardedInstitutions();

    List<PartyProduct> getInstitutionProducts(String institutionId);
}
