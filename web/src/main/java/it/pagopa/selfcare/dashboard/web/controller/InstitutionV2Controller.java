package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.InstitutionV2Service;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2;
import it.pagopa.selfcare.dashboard.web.model.user.UserIdResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping(value = "/v2/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "institutions")
@RequiredArgsConstructor
public class InstitutionV2Controller {

    private final InstitutionV2Service institutionV2Service;
    private final UserV2Service userService;
    private final InstitutionResourceMapper institutionResourceMapper;
    private final UserMapperV2 userMapperV2;

    @GetMapping(value = "/{institutionId}/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionUser}", nickname = "v2RetrieveInstitutionUser")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public InstitutionUserDetailsResource getInstitutionUser(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                                             @PathVariable("institutionId")
                                                             String institutionId,
                                                             @ApiParam("${swagger.dashboard.user.model.id}")
                                                             @PathVariable("userId")
                                                             String userId,
                                                             Authentication authentication) {

        log.trace("getInstitutionUser start");
        String loggedUserId = ((SelfCareUser) authentication.getPrincipal()).getId();

        log.debug("getInstitutionUser institutionId = {}, userId = {}", institutionId, userId);
        UserInfo userInfo = institutionV2Service.getInstitutionUser(institutionId, userId, loggedUserId);
        InstitutionUserDetailsResource result = UserMapper.toInstitutionUserDetails(userInfo);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionUser result = {}", result);
        log.trace("getInstitutionUser end");
        return result;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}", nickname = "v2RetrieveUserInstitutions")
    public List<InstitutionBaseResource> getInstitutions(Authentication authentication) {

        log.trace("getInstitutions start");
        String userId = ((SelfCareUser) authentication.getPrincipal()).getId();
        Collection<InstitutionBase> institutions = userService.getInstitutions(userId);

        List<InstitutionBaseResource> result = institutions.stream()
                .map(institutionResourceMapper::toResource)
                .toList();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");

        return result;
    }

    @PostMapping(value = "/{institutionId}/products/{productId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.createInstitutionProductUser}", nickname = "v2PostCreateInstitutionProductUser")
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
        String userId = userService.createUsers(institutionId, productId, userMapperV2.toUserToCreate(user));
        UserIdResource result = new UserIdResource();
        result.setId(UUID.fromString(userId));
        log.debug("createInstitutionProductUser result = {}", result);
        log.trace("createInstitutionProductUser end");
        return result;
    }

    @PutMapping(value = "/{institutionId}/products/{productId}/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.addUserProductRoles}", nickname = "v2AddUserProductRole")
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
        userService.addUserProductRoles(institutionId, productId, userId, userProductRoles.getProductRoles());
        log.trace("addUserProductRoles end");
    }

    @GetMapping(value = "/{institutionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "${swagger.dashboard.institutions.api.getInstitution}", notes = "${swagger.dashboard.institutions.api.getInstitution}", nickname = "v2GetInstitution")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ANY')")
    public InstitutionResource getInstitution(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                              @PathVariable("institutionId")
                                              String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);

        Institution institution = institutionV2Service.findInstitutionById(institutionId);
        InstitutionResource result = institutionResourceMapper.toResource(institution);

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }
}
