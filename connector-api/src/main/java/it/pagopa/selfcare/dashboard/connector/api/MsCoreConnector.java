package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;

import java.util.List;

public interface MsCoreConnector {

    List<PartyProduct> getInstitutionProducts(String institutionId);

    Institution getInstitution(String institutionId);

    Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updatePnPGInstitutionResource);

    DelegationId createDelegation(DelegationRequest delegation);

    List<BrokerInfo> findInstitutionsByProductAndType(String productId, String type);

    List<Delegation> getDelegations(GetDelegationParameters delegationParameters);

    DelegationWithPagination getDelegationsV2(GetDelegationParameters delegationParameters);

    void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies);

    List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId);

}
