package it.pagopa.selfcare.dashboard.model;

import lombok.Data;

import java.util.List;

@Data
public class ContractGroup {
    private String name;
    private List<ContractInfo> contractsInfo;
}

