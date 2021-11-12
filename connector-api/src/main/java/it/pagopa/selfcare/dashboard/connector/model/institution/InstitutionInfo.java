package it.pagopa.selfcare.dashboard.connector.model.institution;

import java.util.List;

public interface InstitutionInfo {

    String getInstitutionId();

    String getDescription();

    String getDigitalAddress();

    String getStatus();

    List<String> getActiveProducts();

}
