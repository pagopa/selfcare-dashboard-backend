package it.pagopa.selfcare.dashboard.connector.model.delegation;

import lombok.Data;

@Data
public class Delegation {
    private String id;
    private String from;
    private String to;
    private String productId;
    private String institutionFromName;
    private String institutionToName;
    private DelegationType type;
}
