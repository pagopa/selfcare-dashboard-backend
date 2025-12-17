package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.client.IamExternalRestClient;
import it.pagopa.selfcare.dashboard.client.ProductContractApiRestClient;
import it.pagopa.selfcare.dashboard.model.ContractInfo;
import it.pagopa.selfcare.dashboard.model.ContractTemplateUploadRequest;
import it.pagopa.selfcare.dashboard.model.ContractsResponse;
import it.pagopa.selfcare.dashboard.model.mapper.ProductRestClientMapper;
import it.pagopa.selfcare.iam.generated.openapi.v1.dto.ProductRolePermissions;
import it.pagopa.selfcare.iam.generated.openapi.v1.dto.ProductRolePermissionsList;
import it.pagopa.selfcare.product.generated.openapi.v1.dto.ContractTemplateFileType;
import it.pagopa.selfcare.product.generated.openapi.v1.dto.ContractTemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContractServiceImpl implements ContractService {

    private final ProductContractApiRestClient productContractApiRestClient;

    private final ProductRestClientMapper productRestClientMapper;

    private final IamExternalRestClient iamExternalRestClient;

    private static final String VIEW_CONTRACT_PERMISSION = "Selc:ViewContract";

    public static final String ALL = "ALL";

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
                        ProductRestClientMapper productRestClientMapper, IamExternalRestClient iamExternalRestClient) {
        this.productContractApiRestClient = productContractApiRestClient;
        this.productRestClientMapper = productRestClientMapper;
        this.iamExternalRestClient = iamExternalRestClient;
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

        // ===== 1. AUTH / ISSUER CHECK =====
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        String issuer = selfCareUser.getIssuer();
        String userId = selfCareUser.getId();

        if (!"PAGOPA".equalsIgnoreCase(issuer)) {
            throw new AccessDeniedException(
                    String.format("User %s is not allowed to retrieve contracts list", userId));
        }

        // ===== 2. IAM CHECK =====
        List<ProductRolePermissions> allPermissions = Optional.ofNullable(
                        iamExternalRestClient._getIAMProductRolePermissionsList(userId, null).getBody())
                .map(ProductRolePermissionsList::getItems)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new AccessDeniedException(
                        String.format("User %s has no permission", userId)));

        Set<String> explicitProductsWithPermission = allPermissions.stream()
                .filter(p -> !ALL.equalsIgnoreCase(p.getProductId()))
                .filter(p -> p.getPermissions() != null && p.getPermissions().contains(VIEW_CONTRACT_PERMISSION))
                .map(ProductRolePermissions::getProductId)
                .collect(Collectors.toSet());

        Set<String> explicitProductsWithoutPermission = allPermissions.stream()
                .filter(p -> !ALL.equalsIgnoreCase(p.getProductId()))
                .filter(p -> p.getPermissions() == null || !p.getPermissions().contains(VIEW_CONTRACT_PERMISSION))
                .map(ProductRolePermissions::getProductId)
                .collect(Collectors.toSet());

        boolean hasAllPermission = allPermissions.stream()
                .anyMatch(p -> ALL.equalsIgnoreCase(p.getProductId()) &&
                        p.getPermissions() != null &&
                        p.getPermissions().contains(VIEW_CONTRACT_PERMISSION));

        Predicate<String> isAllowed = productId ->
                explicitProductsWithPermission.contains(productId) ||
                        (!explicitProductsWithoutPermission.contains(productId) && hasAllPermission);

        // ===== 3. FETCH CONTRACT TEMPLATES =====
        List<ContractTemplateResponse> templates = Objects.requireNonNull(
                        productContractApiRestClient._listContractTemplates(name, null, version)
                                .getBody())
                .getItems();

        // ===== 4. FILTER + GROUP =====
        Map<String, Map<String, List<ContractInfo>>> contracts = templates.stream()
                .filter(t -> isAllowed.test(t.getProductId()))
                .filter(t -> ContractTemplateFileType.HTML.equals(t.getFileType()))
                .collect(Collectors.groupingBy(
                        ContractTemplateResponse::getProductId,
                        Collectors.groupingBy(
                                ContractTemplateResponse::getName,
                                Collectors.collectingAndThen(
                                        Collectors.mapping(
                                                productRestClientMapper::toContractInfo,
                                                Collectors.toList()
                                        ),
                                        list -> list.stream()
                                                .sorted(VERSION_COMPARATOR)
                                                .toList()
                                )
                        )
                ));

        ContractsResponse response = new ContractsResponse();
        response.setContracts(contracts);
        return response;
    }


}
