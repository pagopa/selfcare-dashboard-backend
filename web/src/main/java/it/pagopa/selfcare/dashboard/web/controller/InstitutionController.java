package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.DelegationService;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.model.*;
import it.pagopa.selfcare.dashboard.web.model.delegation.DelegationResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.*;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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


    @PutMapping(value = "/{institutionId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.saveInstitutionLogo}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ADMIN')")
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


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionBaseResource> getInstitutions(Authentication authentication) {

        log.trace("getInstitutions start");
        String userId = ((SelfCareUser) authentication.getPrincipal()).getId();
        Collection<InstitutionInfo> institutions =  institutionService.getInstitutions(userId);

        List<InstitutionBaseResource> result = institutions.stream()
                .map(institutionResourceMapper::toResource)
                .collect(Collectors.toList());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");

        return result;
    }


    @GetMapping("/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitution}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
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

    @PutMapping("/{institutionId}/geographicTaxonomy")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.updateInstitutionGeographicTaxonomy}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public void updateInstitutionGeographicTaxonomy(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                    @PathVariable("institutionId")
                                                    String institutionId,
                                                    @ApiParam("${swagger.dashboard.institutions.model.geographicTaxonomy}")
                                                    @RequestBody
                                                    @Valid
                                                    GeographicTaxonomyListDto geographicTaxonomyListDto) {
        log.trace("updateInstitutionGeographicTaxonomy start");
        log.debug("updateInstitutionGeographicTaxonomy institutionId = {}, geographic taxonomies = {}", institutionId, geographicTaxonomyListDto);
        GeographicTaxonomyList geographicTaxonomies = new GeographicTaxonomyList();
        geographicTaxonomies.setGeographicTaxonomyList(geographicTaxonomyListDto.getGeographicTaxonomyDtoList().stream().map(GeographicTaxonomyMapper::fromDto).collect(Collectors.toList()));
        institutionService.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        log.trace("updateInstitutionsGeographicTaxonomy end");
    }

    @GetMapping(value = "/{institutionId}/geographicTaxonomy")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionGeographicTaxonomy}")
    public List<GeographicTaxonomyResource> getInstitutionGeographicTaxonomy(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                                             @PathVariable("institutionId")
                                                                             String institutionId) {
        log.trace("getInstitutionGeographicTaxonomy start");
        log.debug("getInstitutionGeographicTaxonomy institutionId = {}", institutionId);
        List<GeographicTaxonomyResource> geographicTaxonomies = institutionService.getGeographicTaxonomyList(institutionId)
                .stream()
                .map(GeographicTaxonomyMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutionGeographicTaxonomy result = {}", geographicTaxonomies);
        log.trace("getInstitutionGeographicTaxonomy end");
        return geographicTaxonomies;
    }

    /**
     * @deprecated since it's not used
     */
    @Deprecated(forRemoval = true, since = "1.5")
    @GetMapping(value = "/{institutionId}/users")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionUsers}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public List<InstitutionUserResource> getInstitutionUsers(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                             @PathVariable("institutionId")
                                                             String institutionId,
                                                             @ApiParam("${swagger.dashboard.products.model.id}")
                                                             @RequestParam(value = "productId", required = false)
                                                             Optional<String> productId,
                                                             @ApiParam("${swagger.dashboard.user.model.role}")
                                                             @RequestParam(value = "role", required = false)
                                                             Optional<SelfCareAuthority> role,
                                                             @ApiParam("${swagger.dashboard.user.model.productRoles}")
                                                             @RequestParam(value = "productRoles", required = false)
                                                             Optional<Set<String>> productRoles) {

        log.trace("getInstitutionUsers start");
        log.debug("getInstitutionUsers institutionId = {}, role = {}, productId = {}", institutionId, role, productId);
        Collection<UserInfo> userInfos = institutionService.getInstitutionUsers(institutionId, productId, role, productRoles);
        List<InstitutionUserResource> result = userInfos.stream()
                .map(UserMapper::toInstitutionUser)
                .collect(Collectors.toList());
        log.debug("getInstitutionUsers result = {}", result);
        log.trace("getInstitutionUsers end");

        return result;
    }


    @GetMapping(value = "/{institutionId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionUser}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public InstitutionUserDetailsResource getInstitutionUser(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                             @PathVariable("institutionId")
                                                                     String institutionId,
                                                             @ApiParam("${swagger.dashboard.user.model.id}")
                                                             @PathVariable("userId")
                                                                     String userId) {

        log.trace("getInstitutionUser start");
        log.debug("getInstitutionUser institutionId = {}, userId = {}", institutionId, userId);
        UserInfo userInfo = institutionService.getInstitutionUser(institutionId, userId);
        InstitutionUserDetailsResource result = UserMapper.toInstitutionUserDetails(userInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionUser result = {}", result);
        log.trace("getInstitutionUser end");

        return result;
    }

    @GetMapping(value = "/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProducts}")
    public List<ProductsResource> getProductsTree() {
        log.trace("getProducts start");
        log.debug("getProducts start");

        List<ProductTree> products = institutionService.getProductsTree();
        List<ProductsResource> result = products.stream()
                .map(ProductsMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getProducts result = {}", result);
        log.debug("getProducts result = {}", result);
        log.trace("getProducts end");

        return result;
    }

    @GetMapping(value = "/{institutionId}/products/{productId}/users")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProductUsers}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ANY')")
    public List<ProductUserResource> getInstitutionProductUsers(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                                @PathVariable("institutionId")
                                                                        String institutionId,
                                                                @ApiParam("${swagger.dashboard.products.model.id}")
                                                                @PathVariable("productId")
                                                                        String productId,
                                                                @ApiParam("${swagger.dashboard.user.model.role}")
                                                                @RequestParam(value = "role", required = false)
                                                                        Optional<SelfCareAuthority> role,
                                                                @ApiParam("${swagger.dashboard.user.model.productRoles}")
                                                                @RequestParam(value = "productRoles", required = false)
                                                                        Optional<Set<String>> productRoles) {

        log.trace("getInstitutionProductUsers start");
        log.debug("getInstitutionProductUsers institutionId = {}, productId = {}, role = {}", institutionId, productId, role);

        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRoles);
        List<ProductUserResource> result = userInfos.stream()
                .map(UserMapper::toProductUser)
                .collect(Collectors.toList());
        log.debug("getInstitutionProductUsers result = {}", result);
        log.trace("getInstitutionProductUsers end");

        return result;
    }


    @PostMapping(value = "/{institutionId}/products/{productId}/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.createInstitutionProductUser}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ADMIN')")
    public UserIdResource createInstitutionProductUser(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                       @PathVariable("institutionId")
                                                               String institutionId,
                                                       @ApiParam("${swagger.dashboard.products.model.id}")
                                                       @PathVariable("productId")
                                                               String productId,
                                                       @ApiParam("${swagger.dashboard.user.model.role}")
                                                       @RequestBody
                                                       @Valid
                                                               CreateUserDto user) {

        log.trace("createInstitutionProductUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createInstitutionProductUser institutionId = {}, productId = {}, user = {}", institutionId, productId, user);
        UserId userId = institutionService.createUsers(institutionId, productId, UserMapper.fromCreateUserDto(user, institutionId));
        UserIdResource result = UserMapper.toIdResource(userId);
        log.debug("createInstitutionProductUser result = {}", result);
        log.trace("createInstitutionProductUser end");
        return result;
    }

    @PutMapping(value = "/{institutionId}/products/{productId}/users/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.addUserProductRoles}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ADMIN')")
    public void addUserProductRoles(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                    @PathVariable("institutionId")
                                            String institutionId,
                                    @ApiParam("${swagger.dashboard.products.model.id}")
                                    @PathVariable("productId")
                                            String productId,
                                    @ApiParam("${swagger.dashboard.user.model.id}")
                                    @PathVariable("userId")
                                            String userId,
                                    @ApiParam("${swagger.dashboard.user.model.productRoles}")
                                    @RequestBody
                                    @Valid
                                            UserProductRoles userProductRoles) {
        log.trace("addUserProductRoles start");
        log.debug("institutionId = {}, productId = {}, userId = {}, userProductRoles = {}", institutionId, productId, userId, userProductRoles);
        institutionService.addUserProductRoles(institutionId, productId, userId, UserMapper.toCreateUserDto(userProductRoles));
        log.trace("addUserProductRoles end");
    }

    @PutMapping(value = "/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.updateInstitutionDescription}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
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
    @GetMapping(value = "/{institutionId}/partners")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public ResponseEntity<List<DelegationResource>> getDelegationsUsingFrom(@ApiParam("${swagger.dashboard.delegation.model.from}")
                                                                   @PathVariable("institutionId") String institutionId,
                                                                   @ApiParam("${swagger.dashboard.delegation.model.productId}")
                                                                   @RequestParam(name = "productId", required = false) String productId) {
        log.trace("getDelegationsUsingFrom start");
        log.debug("getDelegationsUsingFrom institutionId = {}, institutionDto{}", institutionId, productId);
        ResponseEntity<List<DelegationResource>> result = ResponseEntity.status(HttpStatus.OK).body(delegationService.getDelegations(institutionId, null, productId).stream()
                .map(delegationMapper::toDelegationResource)
                .collect(Collectors.toList()));
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
    @GetMapping(value = "/{institutionId}/institutions")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public ResponseEntity<List<DelegationResource>> getDelegationsUsingTo(@ApiParam("${swagger.dashboard.delegation.model.to}")
                                                                   @PathVariable("institutionId") String institutionId,
                                                                   @ApiParam("${swagger.dashboard.delegation.model.productId}")
                                                                   @RequestParam(name = "productId", required = false) String productId) {
        log.trace("getDelegationsUsingTo start");
        log.debug("getDelegationsUsingTo institutionId = {}, institutionDto{}", institutionId, productId);
        ResponseEntity<List<DelegationResource>> result = ResponseEntity.status(HttpStatus.OK).body(delegationService.getDelegations(null, institutionId, productId).stream()
                .map(delegationMapper::toDelegationResource)
                .collect(Collectors.toList()));
        log.debug("getDelegationsUsingTo result = {}", result);
        log.trace("getDelegationsUsingTo end");
        return result;

    }
}
