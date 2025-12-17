package it.pagopa.selfcare.dashboard.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.model.ContractTemplateUploadRequest;
import it.pagopa.selfcare.dashboard.model.ContractsResponse;
import it.pagopa.selfcare.dashboard.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.io.InputStream;


@Slf4j
@RestController
@RequestMapping(value = "/v1/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "contracts")
@RequiredArgsConstructor
@Validated
public class ContractController {

    private final ContractService contractService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "uploadContractTemplateUsingPOST", notes = "Upload a new contract template version", nickname = "postUploadContract")
    //@PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(null, #productId, null), 'Selc:ViewContract')")
    public void uploadContract(@ApiParam("${swagger.dashboard.products.model.id}")
                                                       @RequestParam(value = "productId")
                                                       String productId,
                                                       @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "The field can only contain letters, numbers, and spaces")
                                                       @RequestParam(value = "name")
                                                       String name,
                                                       @RequestParam(value = "description", required = false)
                                                       String description,
                                                       @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "The format must be X.X.X where X is a number")
                                                       @RequestParam(value = "version")
                                                       String version,
                                                       @RequestPart MultipartFile file,
                                                       Authentication authentication) {
        String loggedUserId = ((SelfCareUser) authentication.getPrincipal()).getId();
        ContractTemplateUploadRequest request = ContractTemplateUploadRequest.builder()
                .file(file)
                .name(name)
                .productId(productId)
                .description(description)
                .version(version)
                .createdBy(loggedUserId)
                .build();
        contractService.uploadContract(productId, request);
    }

    @GetMapping(value = "/{contractId}",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "downloadContractTemplateUsingGET", notes = "Download a contract template version", nickname = "downloadContractTemplate")
    //@PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(null, #productId, null), 'Selc:ViewContract')")
    public ResponseEntity<byte[]> downloadContract(@ApiParam("${swagger.dashboard.contract.model.id}")
                                                 @PathVariable("contractId")
                                                 String contractId,
                                                 @ApiParam("${swagger.dashboard.products.model.id}")
                                                 @RequestParam(value = "productId")
                                                 String productId,
                                                 @ApiParam("${swagger.dashboard.contract.model.name}")
                                                 @RequestParam(value = "contractName")
                                                 String contractName
                                                 ) throws IOException {
        Resource contract = contractService.downloadContract(contractId, productId);
        return getResponseEntity(contract, contractName);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "getContractsListUsingGET", notes = "List contract templates available", nickname = "listContractTemplates")
    public ContractsResponse getContractsList(@RequestParam(value = "name", required = false)
                                                         String name,
                                              @RequestParam(value = "version", required = false)
                                                         String version
    ) {
        return contractService.listContractTemplates(name, version);
    }

    private ResponseEntity<byte[]> getResponseEntity(Resource contract, String contractName) throws IOException {
        try (InputStream inputStream = contract.getInputStream()) {
            byte[] byteArray = IOUtils.toByteArray(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

            String filename = contractName != null ? contractName.concat(".html") : "contract.html";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(byteArray);
        }
    }


}
