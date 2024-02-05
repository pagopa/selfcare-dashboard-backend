package it.pagopa.selfcare.dashboard.connector.api;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;

import java.util.List;


public interface UserApiConnector {

    void updateUser(String userId, String institutionId, MutableUserFieldsDto userDto);

    List<InstitutionInfo> getUserProducts(String userId);

}
