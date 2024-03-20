package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.List;

@Data
public class UserInstitution {

    private String id;
    private String userId;
    private String institutionId;
    private String institutionDescription;
    private String institutionRootName;
    private String userMailUuid;
    private List<OnboardedProduct> products = null;
}
