package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "user")
@RequestMapping(value = "/v2", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserV2Controller {

    private final UserV2Service userService;
    private final InstitutionResourceMapper institutionResourceMapper;

    @GetMapping("/institutions")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutions}")
    public List<InstitutionBaseResource> getInstitutions(Authentication authentication) {

        log.trace("getInstitutions start");
        String userId = ((SelfCareUser) authentication.getPrincipal()).getId();
        Collection<InstitutionInfo> institutions = userService.getInstitutions(userId);

        List<InstitutionBaseResource> result = institutions.stream()
                .map(institutionResourceMapper::toResource)
                .toList();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");

        return result;
    }

    @PostMapping(value = "/{userId}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.suspendUser}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ADMIN')")
    public void suspendRelationship(@ApiParam("${swagger.dashboard.user.model.id}")
                                    @PathVariable("userId") String userId,
                                    @ApiParam("${swagger.dashboard.support.model.institutionId}")
                                    @RequestParam(value = "institutionId") String institutionId,
                                    @RequestParam(value = "productId") String productId) {

        log.trace("suspendUser start");
        log.debug("suspendUser {} for institution: {} and product: {}", userId, institutionId, productId);
        userService.suspendUserProduct(userId, institutionId, productId);
        log.trace("suspendUser end");

    }


    @PostMapping(value = "/{userId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.activateUser}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ADMIN')")
    public void activateRelationship(@ApiParam("${swagger.dashboard.user.model.id}")
                                     @PathVariable("userId") String userId,
                                     @RequestParam(value = "institutionId") String institutionId,
                                     @RequestParam(value = "productId") String productId) {

        log.trace("activateUser start");
        log.debug("activateUser {} for institution: {} and product: {}", userId, institutionId, productId);
        userService.activateUserProduct(userId, institutionId, productId);
        log.trace("activateUser end");

    }

    @DeleteMapping(value = "/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.deleteUser}")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.ProductAclDomain(#institutionId, #productId), 'ADMIN')")
    public void deleteRelationshipById(@ApiParam("${swagger.dashboard.user.model.id}")
                                       @PathVariable("userId") String userId,
                                       @RequestParam(value = "institutionId") String institutionId,
                                       @RequestParam(value = "productId") String productId) {
        log.trace("deleteUser start");
        log.debug("deleteUser {} for institution: {} and product: {}", userId, institutionId, productId);
        userService.deleteUserProduct(userId, institutionId, productId);
        log.trace("deleteUser end");
    }
}
