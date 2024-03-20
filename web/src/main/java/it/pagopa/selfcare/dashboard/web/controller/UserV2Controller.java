package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.UserV2Service;
import it.pagopa.selfcare.dashboard.web.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.web.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.mapper.InstitutionResourceMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.web.model.mapper.UserMapperV2;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
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

@Slf4j
@RestController
@Api(tags = "user")
@RequestMapping(value = "/v2/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserV2Controller {

    private final UserV2Service userService;
    private final UserMapperV2 userMapperV2;

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

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.getUserByInternalId}")
    public UserResource getUserById(@ApiParam("${swagger.dashboard.user.model.id}")
                                    @PathVariable("id") String userId) {
        log.trace("getUserById start");
        log.debug("getUserById id = {}", userId);
        User user = userService.getUserById(userId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserById = {}", user);
        log.trace("getUserById end");
        return userMapperV2.toUserResource(user);
    }

    @PostMapping(value = "/search")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.search}")
    @ApiResponse(responseCode = "404",
            description = "Not Found",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    public UserResource search(@ApiParam("${swagger.dashboard.user.model.searchUserDto}")
                               @RequestBody
                               @Valid
                               SearchUserDto searchUserDto) {
        log.trace("searchByFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode fiscalCode = {}", searchUserDto);
        User user = userService.searchUserByFiscalCode(searchUserDto.getFiscalCode());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode user = {}", user);
        log.trace("searchByFiscalCode end");
        return userMapperV2.toUserResource(user);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.updateUserById}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ADMIN')")
    public void updateUser(@ApiParam("${swagger.dashboard.user.model.id}")
                           @PathVariable("id")
                           String userId,
                           @ApiParam("${swagger.dashboard.institutions.model.id}")
                           @RequestParam(value = "institutionId")
                           String institutionId,
                           @RequestBody
                           @Valid
                           UpdateUserDto updateUserDto) {
        log.trace("updateUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "userId = {}, institutionId = {}, userDto = {}", userId, institutionId, updateUserDto);
        userService.updateUser(userId, institutionId, userMapperV2.fromUpdateUser(institutionId, updateUserDto));
        log.trace("updateUser end");
    }

    @GetMapping(value = "/institution/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionUsers}")
    @PreAuthorize("hasPermission(#institutionId, 'InstitutionResource', 'ADMIN')")
    public List<ProductUserResource> getUsers(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                              @PathVariable("institutionId") String institutionId,
                                              @RequestParam(value = "productId", required = false) String productId,
                                              Authentication authentication) {
        log.trace("getUsers start");
        log.debug("getUsers for institution: {} and product: {}", institutionId, productId);
        String loggedUserId = ((SelfCareUser) authentication.getPrincipal()).getId();

        Collection<UserInfo> userInfos = userService.getUsersByInstitutionId(institutionId, productId, loggedUserId);
        List<ProductUserResource> result = userInfos.stream()
                .map(UserMapper::toProductUser)
                .toList();
        log.debug("getUsers result = {}", result);
        log.trace("getUsers end");

        return result;
    }
}
