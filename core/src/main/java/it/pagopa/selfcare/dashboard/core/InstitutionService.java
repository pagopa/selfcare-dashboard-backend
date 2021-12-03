package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;

import java.util.List;

public interface InstitutionService {

    InstitutionInfo getInstitution(String institutionId);

    List<Product> getInstitutionProducts(String institutionId);
}
