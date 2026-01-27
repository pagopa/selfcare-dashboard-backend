package it.pagopa.selfcare.dashboard.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.model.GeographicTaxonomyListDto;
import it.pagopa.selfcare.dashboard.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.model.UpdateInstitutionDto;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationResource;
import it.pagopa.selfcare.dashboard.model.delegation.GetDelegationParameters;
import it.pagopa.selfcare.dashboard.model.delegation.Order;
import it.pagopa.selfcare.dashboard.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.model.institution.Institution;
import it.pagopa.selfcare.dashboard.model.mapper.DelegationMapper;
import it.pagopa.selfcare.dashboard.model.mapper.GeographicTaxonomyMapper;
import it.pagopa.selfcare.dashboard.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.dashboard.model.mapper.ProductsMapper;
import it.pagopa.selfcare.dashboard.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.model.product.ProductsResource;
import it.pagopa.selfcare.dashboard.service.DelegationService;
import it.pagopa.selfcare.dashboard.service.FileStorageService;
import it.pagopa.selfcare.dashboard.service.InstitutionService;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(value = "/v1/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final FileStorageService storageService;
    private final InstitutionService institutionService;
    private final InstitutionResourceMapper institutionResourceMapper;
    private final DelegationService delegationService;
    private final DelegationMapper delegationMapper;


    @Autowired
    public InstitutionController(FileStorageService storageService, InstitutionService institutionService, InstitutionResourceMapper institutionResourceMapper, DelegationService delegationService, DelegationMapper delegationMapper) {
        this.storageService = storageService;
        this.institutionService = institutionService;
        this.institutionResourceMapper = institutionResourceMapper;
        this.delegationService = delegationService;
        this.delegationMapper = delegationMapper;
    }


    @PutMapping(value = "/{institutionId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.saveInstitutionLogo}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:UploadLogo')")
    public Object saveInstitutionLogo(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                      @PathVariable("institutionId") String institutionId,
                                      @ApiParam("${swagger.dashboard.institutions.model.logo}")
                                      @RequestPart("logo") MultipartFile logo) throws IOException {

        log.trace("saveInstitutionLogo start");
        log.debug("saveInstitutionLogo institutionId = {}, logo = {}", institutionId, logo);

        storageService.storeInstitutionLogo(institutionId, logo.getInputStream(), logo.getContentType(), logo.getOriginalFilename());
        log.trace("saveInstitutionLogo end");

        return null;
    }


    @GetMapping(value = "/{institutionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitution}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:ViewInstitutionData')")
    public InstitutionResource getInstitution(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                              @PathVariable("institutionId")
                                                      String institutionId) {

        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        Institution institution = institutionService.findInstitutionById(institutionId);
        InstitutionResource result = institutionResourceMapper.toResource(institution);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");

        return result;
    }

    @PutMapping(value = "/{institutionId}/geographic-taxonomy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.updateInstitutionGeographicTaxonomy}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:UpdateGeoTaxonomy')")
    public void updateInstitutionGeographicTaxonomy(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                    @PathVariable("institutionId")
                                                    String institutionId,
                                                    @ApiParam("${swagger.dashboard.institutions.model.geographicTaxonomy}")
                                                    @RequestBody
                                                    @Valid
                                                    GeographicTaxonomyListDto geographicTaxonomyListDto) {
        log.trace("updateInstitutionGeographicTaxonomy start");
        log.debug("updateInstitutionGeographicTaxonomy institutionId = {}, geographic taxonomies = {}", Encode.forJava(institutionId), Encode.forJava(geographicTaxonomyListDto.toString()));
        GeographicTaxonomyList geographicTaxonomies = new GeographicTaxonomyList();
        geographicTaxonomies.setGeographicTaxonomyList(geographicTaxonomyListDto.getGeographicTaxonomyDtoList().stream().map(GeographicTaxonomyMapper::fromDto).toList());
        institutionService.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        log.trace("updateInstitutionsGeographicTaxonomy end");
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProducts}")
    public List<ProductsResource> getProductsTree() {
        log.trace("getProducts start");
        log.debug("getProducts start");

        List<ProductTree> products = institutionService.getProductsTree();
        List<ProductsResource> result = products.stream()
                .map(ProductsMapper::toResource)
                .toList();
        log.debug("getProducts result = {}", result);
        log.debug("getProducts result = {}", result);
        log.trace("getProducts end");

        return result;
    }

    @PutMapping(value = "/{institutionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.updateInstitutionDescription}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:UpdateInstitutionData')")
    public Institution updateInstitutionDescription(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                    @PathVariable("institutionId")
                                                    String institutionId,
                                                    @RequestBody
                                                    @Valid
                                                    UpdateInstitutionDto institutionDto) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, institutionDto{}", institutionId, institutionDto);
        Institution result = institutionService.updateInstitutionDescription(institutionId, institutionResourceMapper.toUpdateResource(institutionDto));
        log.debug("updateInstitutionDescription result = {}", result);
        log.trace("updateInstitutionDescription end");
        return result;
    }

    /**
     * The function get institution's delegation
     *
     * @param institutionId String
     * @return InstitutionResponse
     * * Code: 200, Message: successful operation, DataType: List<DelegationResponse>
     * * Code: 404, Message: Institution data not found, DataType: Problem
     * * Code: 400, Message: Bad Request, DataType: Problem
     */
    @ApiOperation(value = "${swagger.dashboard.institutions.partners}", notes = "${swagger.dashboard.institutions.partners}")
    @GetMapping(value = "/{institutionId}/partners", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(#institutionId, #productId, null), 'Selc:ViewDelegations')")
    public ResponseEntity<List<DelegationResource>> getDelegationsUsingFrom(@ApiParam("${swagger.dashboard.delegation.model.from}")
                                                                   @PathVariable("institutionId") String institutionId,
                                                                   @ApiParam("${swagger.dashboard.delegation.model.productId}")
                                                                   @RequestParam(name = "productId", required = false) String productId) {
        log.trace("getDelegationsUsingFrom start");
        log.debug("getDelegationsUsingFrom institutionId = {}, institutionDto{}", institutionId, productId);
        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .from(institutionId)
                .productId(productId)
                .build();

        ResponseEntity<List<DelegationResource>> result = ResponseEntity.status(HttpStatus.OK).body(delegationService.getDelegations(delegationParameters).stream()
                .map(delegationMapper::toDelegationResource)
                .toList());
        log.debug("getDelegationsUsingFrom result = {}", result);
        log.trace("getDelegationsUsingFrom end");
        return result;

    }

    /**
     * The function get the list of delegation by the partners
     *
     * @param institutionId String
     * @return InstitutionResponse
     * * Code: 200, Message: successful operation, DataType: List<DelegationResponse>
     * * Code: 404, Message: Institution data not found, DataType: Problem
     * * Code: 400, Message: Bad Request, DataType: Problem
     */
    @ApiOperation(value = "${swagger.dashboard.institutions.delegations}", notes = "${swagger.dashboard.institutions.delegations}")
    @GetMapping(value = "/{institutionId}/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.security.FilterAuthorityDomain(#institutionId, #productId, null), 'Selc:ViewDelegations')")
    public ResponseEntity<List<DelegationResource>> getDelegationsUsingTo(@ApiParam("${swagger.dashboard.delegation.model.to}")
                                                                   @PathVariable("institutionId") String institutionId,
                                                                   @ApiParam("${swagger.dashboard.delegation.model.productId}")
                                                                   @RequestParam(name = "productId", required = false) String productId,
                                                                   @ApiParam("${swagger.dashboard.delegation.model.description}")
                                                                   @RequestParam(name = "search", required = false) String search,
                                                                   @ApiParam("${swagger.dashboard.delegation.delegations.order}")
                                                                   @RequestParam(name = "order", required = false) Order order,
                                                                   @RequestParam(name = "page", required = false) Integer page,
                                                                   @RequestParam(name = "size", required = false) Integer size) {
        log.trace("getDelegationsUsingTo start");
        log.debug("getDelegationsUsingTo institutionId = {}, institutionDto{}", institutionId, productId);

        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .to(institutionId)
                .productId(productId)
                .search(search)
                .order(Objects.nonNull(order) ? order.name() : null)
                .page(page)
                .size(size)
                .build();

        ResponseEntity<List<DelegationResource>> result = ResponseEntity.status(HttpStatus.OK).body(delegationService.getDelegations(delegationParameters).stream()
                .map(delegationMapper::toDelegationResource)
                .toList());
        log.debug("getDelegationsUsingTo result = {}", result);
        log.trace("getDelegationsUsingTo end");
        return result;

    }
}
