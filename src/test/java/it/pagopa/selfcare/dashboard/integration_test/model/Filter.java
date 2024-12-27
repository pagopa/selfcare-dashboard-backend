package it.pagopa.selfcare.dashboard.integration_test.model;

import it.pagopa.selfcare.dashboard.model.delegation.Order;
import lombok.Data;

import java.util.List;

@Data
public class Filter {

    private String environment;
    private String lang;
    private String institutionId;
    private String productId;
    private String userId;
    private String groupId;
    private List<String> states;
    private Integer page;
    private Integer size;
    private String sort;
    private String institutionType;
    private List<String> roles;
    private List<String> productRoles;
    private Order order;
    private String search;
    private String status;
}
