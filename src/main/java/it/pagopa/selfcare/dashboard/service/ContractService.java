package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.dashboard.model.ContractTemplateUploadRequest;
import it.pagopa.selfcare.dashboard.model.ContractsResponse;
import org.springframework.core.io.Resource;

public interface ContractService {

    void uploadContract(String productId, ContractTemplateUploadRequest contractUploadRequest);

    Resource downloadContract(String contractId, String productId);

    ContractsResponse listContractTemplates(String name, String version);
}
