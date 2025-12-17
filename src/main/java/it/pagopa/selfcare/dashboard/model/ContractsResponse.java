package it.pagopa.selfcare.dashboard.model;

import lombok.Data;

import java.util.List;

@Data
public class ContractsResponse {
    private List<ProductContracts> contracts;
}