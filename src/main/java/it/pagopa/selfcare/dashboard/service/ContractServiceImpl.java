package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.dashboard.client.ProductContractApiRestClient;
import it.pagopa.selfcare.dashboard.model.*;
import it.pagopa.selfcare.dashboard.model.mapper.ProductRestClientMapper;
import it.pagopa.selfcare.product.generated.openapi.v1.dto.ContractTemplateFileType;
import it.pagopa.selfcare.product.generated.openapi.v1.dto.ContractTemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContractServiceImpl implements ContractService {

    private final ProductContractApiRestClient productContractApiRestClient;

    private final ProductRestClientMapper productRestClientMapper;

    private static int[] versionKey(String version) {
        return Arrays.stream(version.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static final Comparator<ContractInfo> VERSION_COMPARATOR =
            Comparator.comparing(
                    ci -> versionKey(ci.getContractTemplateVersion()),
                    (a, b) -> {
                        for (int i = 0; i < Math.max(a.length, b.length); i++) {
                            int x = i < a.length ? a[i] : 0;
                            int y = i < b.length ? b[i] : 0;
                            int cmp = Integer.compare(x, y);
                            if (cmp != 0) return cmp;
                        }
                        return 0;
                    }
            );



    @Autowired
    ContractServiceImpl(ProductContractApiRestClient productContractApiRestClient,
                        ProductRestClientMapper productRestClientMapper) {
        this.productContractApiRestClient = productContractApiRestClient;
        this.productRestClientMapper = productRestClientMapper;
    }



    @Override
    public void uploadContract(String productId, ContractTemplateUploadRequest uploadRequest) {
        log.trace("uploadContract start");
        log.debug("uploadContract productId = {}, version = {}", Encode.forJava(productId), Encode.forJava(uploadRequest.getVersion()));
        productContractApiRestClient._uploadContractTemplate(uploadRequest.getName(), uploadRequest.getProductId(), uploadRequest.getVersion(), uploadRequest.getFile(), uploadRequest.getCreatedBy(), uploadRequest.getDescription());
    }

    @Override
    public Resource downloadContract(String contractId, String productId) {
        log.trace("downloadContract start");
        log.debug("downloadContract contractId = {}, productId = {}", Encode.forJava(contractId), Encode.forJava(productId));
        ResponseEntity<Resource> contractTemplate = productContractApiRestClient._downloadContractTemplate(contractId, null, productId);
        log.trace("downloadContract end");
        return contractTemplate.getBody();
    }

    @Override
    public ContractsResponse listContractTemplates(String name, String version) {
        log.trace("listContractTemplates start");
        log.debug("listContractTemplates name = {}, version = {}",
                Encode.forJava(name), Encode.forJava(version));

        List<ContractTemplateResponse> templates =
                Objects.requireNonNull(
                        productContractApiRestClient
                                ._listContractTemplates(name, null, version)
                                .getBody()
                ).getItems();

        // HTML filter
        List<ProductContracts> contracts = templates.stream()
                .filter(t -> ContractTemplateFileType.HTML.equals(t.getFileType()))
                .collect(Collectors.groupingBy(ContractTemplateResponse::getProductId))
                .entrySet()
                .stream()
                .map(productEntry -> {
                    String productId = productEntry.getKey();
                    List<ContractTemplateResponse> productTemplates = productEntry.getValue();

                    List<ContractGroup> contractGroups = productTemplates.stream()
                            .collect(Collectors.groupingBy(ContractTemplateResponse::getName))
                            .entrySet()
                            .stream()
                            .map(contractEntry -> {
                                String contractName = contractEntry.getKey();
                                List<ContractInfo> sortedContracts = contractEntry.getValue().stream()
                                        .map(productRestClientMapper::toContractInfo)
                                        .sorted(VERSION_COMPARATOR)
                                        .toList();
                                ContractGroup group = new ContractGroup();
                                group.setName(contractName);
                                group.setContractsInfo(sortedContracts);
                                return group;
                            })
                            .toList();

                    ProductContracts pc = new ProductContracts();
                    pc.setProductId(productId);
                    pc.setContracts(contractGroups);
                    return pc;
                })
                .toList();

        ContractsResponse response = new ContractsResponse();
        response.setContracts(contracts);
        return response;
    }


}
