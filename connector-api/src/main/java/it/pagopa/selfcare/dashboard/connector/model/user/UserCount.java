package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.List;

@Data
public class UserCount {

    private String institutionId;
    private String productId;
    private List<String> roles;
    private List<String> status;
    private Long count;

}
