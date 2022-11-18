package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.UserId;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.FileStorageService;
import it.pagopa.selfcare.dashboard.core.InstitutionService;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.ProductsMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.product.ProductsResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
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
import java.util.Set;
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

        log.trace("saveInstitutionLogo start");
        log.debug("saveInstitutionLogo institutionId = {}, logo = {}", institutionId, logo);

        storageService.storeInstitutionLogo(institutionId, logo.getInputStream(), logo.getContentType(), logo.getOriginalFilename());
        log.trace("saveInstitutionLogo end");

        return null;
    }


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionResource> getInstitutions() {

        log.trace("getInstitutions start");

        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        List<InstitutionResource> result = institutions.stream()
                .map(InstitutionMapper::toResource)
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

        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        InstitutionResource result = InstitutionMapper.toResource(institutionInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");

        return result;
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


    @GetMapping(value = "/{institutionId}/products")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionProducts}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public List<ProductsResource> getInstitutionProducts(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                         @PathVariable("institutionId")
                                                                 String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);

        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        List<ProductsResource> result = products.stream()
                .map(ProductsMapper::toResource)
                .collect(Collectors.toList());
        log.debug("getInstitutionProducts result = {}", result);
        log.trace("getInstitutionProducts end");

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

}
