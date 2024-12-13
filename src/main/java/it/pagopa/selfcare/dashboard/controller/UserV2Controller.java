package it.pagopa.selfcare.dashboard.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.dashboard.model.user.User;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.service.UserV2Service;
import it.pagopa.selfcare.dashboard.model.SearchUserDto;
import it.pagopa.selfcare.dashboard.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapperV2;
import it.pagopa.selfcare.dashboard.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.model.user.UserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "user")
@RequestMapping(value = "/v2/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserV2Controller {

    private final UserV2Service userService;
    private final UserMapper userMapper;
    private final UserMapperV2 userMapperV2;

    @PostMapping(value = "/{userId}/suspend", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.suspendUser}", nickname = "v2SuspendRelationshipUsingPOST")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, #productId, null), 'Selc:ManageProductUsers')")
    public void suspendRelationship(@ApiParam("${swagger.dashboard.user.model.id}")
                                    @PathVariable("userId") String userId,
                                    @ApiParam("${swagger.dashboard.support.model.institutionId}")
                                    @RequestParam(value = "institutionId") String institutionId,
                                    @RequestParam(value = "productId") String productId,
                                    @RequestParam(value = "productRole", required = false) String productRole) {

        log.trace("suspendUser start");
        log.debug("suspendUser {} for institution: {}, productId: {} and productRole: {}", userId, institutionId, productId, productRole);
        userService.suspendUserProduct(userId, institutionId, productId, productRole);
        log.trace("suspendUser end");

    }

    @PostMapping(value = "/{userId}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.activateUser}", nickname = "v2ActivateRelationshipUsingPOST")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, #productId, null), 'Selc:ManageProductUsers')")
    public void activateRelationship(@ApiParam("${swagger.dashboard.user.model.id}")
                                     @PathVariable("userId") String userId,
                                     @RequestParam(value = "institutionId") String institutionId,
                                     @RequestParam(value = "productId") String productId,
                                     @RequestParam(value = "productRole", required = false) String productRole) {

        log.trace("activateUser start");
        log.debug("activateUser {} for institution: {}, productId: {} and productRole: {}", userId, institutionId, productId, productRole);
        userService.activateUserProduct(userId, institutionId, productId, productRole);
        log.trace("activateUser end");

    }

    @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.deleteUser}", nickname = "v2DeleteRelationshipByIdUsingDELETE")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, #productId, null), 'Selc:ManageProductUsers')")
    public void deleteRelationshipById(@ApiParam("${swagger.dashboard.user.model.id}")
                                       @PathVariable("userId") String userId,
                                       @RequestParam(value = "institutionId") String institutionId,
                                       @RequestParam(value = "productId") String productId,
                                       @RequestParam(value = "productRole", required = false) String productRole) {
        log.trace("deleteUser start");
        log.debug("deleteUser {} for institution: {}, productId: {} and productRole: {}", userId, institutionId, productId, productRole);
        userService.deleteUserProduct(userId, institutionId, productId, productRole);
        log.trace("deleteUser end");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.getUserByInternalId}", nickname = "v2GetUserByIdUsingGET")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:ManageProductUsers')")
    public UserResource getUserById(@ApiParam("${swagger.dashboard.user.model.id}")
                                    @PathVariable("id") String userId,
                                    @ApiParam("${swagger.dashboard.institutions.model.id}")
                                    @RequestParam(value = "institutionId")
                                    String institutionId,
                                    @ApiParam("${swagger.dashboard.user.model.fields}")
                                    @RequestParam(value = "fields", required = false)
                                    List<String> fields) {
        log.trace("getUserById start");
        log.debug("getUserById id = {}", userId);
        User user = userService.getUserById(userId, institutionId, fields);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserById = {}", user);
        log.trace("getUserById end");
        return userMapperV2.toUserResource(user);
    }

    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.search}", nickname = "v2SearchUserByFiscalCodeUsingPOST")
    @ApiResponse(responseCode = "404",
            description = "Not Found",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:ManageProductUsers')")
    public UserResource search(@ApiParam("${swagger.dashboard.user.model.searchUserDto}")
                               @RequestBody
                               @Valid
                               SearchUserDto searchUserDto,
                               @ApiParam("${swagger.dashboard.institutions.model.id}")
                               @RequestParam(value = "institutionId")
                               String institutionId) {
        log.trace("searchByFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode fiscalCode = {}", searchUserDto);
        User user = userService.searchUserByFiscalCode(searchUserDto.getFiscalCode(), institutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode user = {}", user);
        log.trace("searchByFiscalCode end");
        return userMapperV2.toUserResource(user);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.user.api.updateUserById}", nickname = "v2UpdateUserUsingPUT")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, null, null), 'Selc:ManageProductUsers')")
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
        userService.updateUser(userId, institutionId, userMapperV2.fromUpdateUser(updateUserDto));
        log.trace("updateUser end");
    }

    @GetMapping(value = "/institution/{institutionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.getInstitutionUsers}", nickname = "v2GetUsersUsingGET")
    @PreAuthorize("hasPermission(new it.pagopa.selfcare.dashboard.web.security.FilterAuthorityDomain(#institutionId, #productId, null), 'Selc:ListProductUsers')")
    public List<ProductUserResource> getUsers(@ApiParam("${swagger.dashboard.institutions.model.id}")
                                              @PathVariable("institutionId") String institutionId,
                                              @RequestParam(value = "productId", required = false) String productId,
                                              @RequestParam(value = "productRoles", required = false) List<String> productRoles,
                                              @RequestParam(value = "roles", required = false) List<String> roles,
                                              Authentication authentication) {
        log.trace("getUsers start");
        log.debug("getUsers for institution: {} and product: {}", institutionId, productId);
        String loggedUserId = ((SelfCareUser) authentication.getPrincipal()).getId();

        List<ProductUserResource> response = new ArrayList<>();
        Collection<UserInfo> userInfos = userService.getUsersByInstitutionId(institutionId, productId, productRoles, roles, loggedUserId);
        userInfos.forEach(userInfo -> response.addAll(userMapper.toProductUsers(userInfo)));
        log.debug("getUsers result = {}", response);
        log.trace("getUsers end");

        return response;
    }
}
