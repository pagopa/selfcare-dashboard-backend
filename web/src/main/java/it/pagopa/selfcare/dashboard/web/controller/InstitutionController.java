package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.model.*;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
public class InstitutionController {

    private final FileStorageService storageService;
    private final InstitutionService institutionService;


    @Autowired
    public InstitutionController(FileStorageService storageService, InstitutionService institutionService) {
        this.storageService = storageService;
        this.institutionService = institutionService;
    }


    @PutMapping(value = "/{institutionId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.saveInstitutionLogo}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ADMIN')")
    public Object saveInstitutionLogo(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                      @PathVariable("institutionId") String institutionId,
                                      @ApiParam("${swagger.dashboard.institutions.model.logo}")
                                      @RequestPart("logo") MultipartFile logo) throws IOException {
        if (log.isDebugEnabled()) {
            log.trace("saveInstitutionLogo");
            log.debug("institutionId = {}, logo = {}", institutionId, logo);
        }
        storageService.storeInstitutionLogo(institutionId, logo.getInputStream(), logo.getContentType(), logo.getOriginalFilename());
        return null;
    }


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions() {
        if (log.isTraceEnabled()) {
            log.trace("getInstitutions");
        }
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        return institutions.stream()
                .map(InstitutionMapper::toResource)
                .collect(Collectors.toList());
    }


    @GetMapping("/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitution}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public InstitutionResource getInstitution(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                              @PathVariable("institutionId")
                                                      String institutionId) {
        if (log.isDebugEnabled()) {
            log.trace("getInstitution");
            log.debug("institutionId = {}", institutionId);
        }
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        return InstitutionMapper.toResource(institutionInfo);
    }


    @GetMapping(value = "/{institutionId}/users")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProductUsers}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public List<InstitutionUserResource> getInstitutionUsers(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                             @PathVariable("institutionId")
                                                                     String institutionId,
                                                             @ApiParam("${swagger.dashboard.products.model.id}")
                                                             @RequestParam(value = "productId", required = false)
                                                                     Optional<String> productId,
                                                             @ApiParam("${swagger.dashboard.user.model.role}")
                                                             @RequestParam(value = "role", required = false)
                                                                     Optional<SelfCareAuthority> role) {
        if (log.isDebugEnabled()) {
            log.trace("getInstitutionUsers");
            log.debug("institutionId = {}, role = {}, productId = {}", institutionId, role, productId);
        }
        Collection<UserInfo> userInfos = institutionService.getInstitutionUsers(institutionId, productId, role);
        return userInfos.stream()
                .map(UserMapper::toInstitutionUser)
                .collect(Collectors.toList());
    }


    @GetMapping(value = "/{institutionId}/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProducts}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public List<ProductsResource> getInstitutionProducts(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                         @PathVariable("institutionId")
                                                                 String institutionId) {
        if (log.isDebugEnabled()) {
            log.trace("getInstitutionProducts");
            log.debug("institutionId = {}", institutionId);
        }
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        return products.stream()
                .map(ProductsMapper::toResource)
                .collect(Collectors.toList());
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
                                                                        Optional<SelfCareAuthority> role) {
        if (log.isDebugEnabled()) {
            log.trace("getInstitutionProductUsers");
            log.debug("institutionId = {}, productId = {}, role = {}", institutionId, productId, role);
        }
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role);
        return userInfos.stream()
                .map(UserMapper::toProductUser)
                .collect(Collectors.toList());
    }


    @PostMapping(value = "/{institutionId}/products/{productId}/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.createInstitutionProductUser}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ADMIN')")
    public void createInstitutionProductUser(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                             @PathVariable("institutionId")
                                                     String institutionId,
                                             @ApiParam("${swagger.dashboard.products.model.id}")
                                             @PathVariable("productId")
                                                     String productId,
                                             @ApiParam("${swagger.dashboard.user.model.role}")
                                             @RequestBody
                                             @Valid
                                                     CreateUserDto user) {
        if (log.isDebugEnabled()) {
            log.trace("createInstitutionProductUser");
            log.debug("institutionId = {}, productId = {}, user = {}", institutionId, productId, user);
        }

        institutionService.createUsers(institutionId, productId, UserMapper.fromCreateUserDto(user));
    }

}
