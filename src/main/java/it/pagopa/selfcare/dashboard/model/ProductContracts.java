package it.pagopa.selfcare.dashboard.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductContracts {
    private String productId;
    private List<ContractGroup> contracts;
}
