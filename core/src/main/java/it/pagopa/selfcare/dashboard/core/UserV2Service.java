package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.*;

import java.util.Collection;
import java.util.UUID;

public interface UserV2Service {

    void updateUser(UUID id, String institutionId, MutableUserFieldsDto userDto);

    Collection<InstitutionInfo> getInstitutions(String userId);


}
