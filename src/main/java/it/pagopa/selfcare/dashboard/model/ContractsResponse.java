package it.pagopa.selfcare.dashboard.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ContractsResponse {

    private Map<String, Map<String, List<ContractInfo>>> contracts;
}
