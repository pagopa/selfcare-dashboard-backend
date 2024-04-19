package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.UserPermissionRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.UserMapper;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.CreateUserDto;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConnectorImpl implements UserApiConnector {

    private final UserApiRestClient userApiRestClient;
    private final UserInstitutionApiRestClient userInstitutionApiRestClient;
    private final UserPermissionRestClient userPermissionRestClient;
    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;

    static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";

    @Override
    public List<InstitutionBase> getUserInstitutions(String userId) {
        log.trace("getUserProducts start");
        UserInfoResponse userInfoResponse = userApiRestClient._usersUserIdInstitutionsGet(userId, null,
                List.of(ACTIVE.name(), PENDING.name(), TOBEVALIDATED.name())).getBody();

        if(Objects.isNull(userInfoResponse) ||
                Objects.isNull(userInfoResponse.getInstitutions())) return List.of();

        List<InstitutionBase> result = userInfoResponse.getInstitutions().stream()
                .map(institutionMapper::toInstitutionBase)
                .sorted(Comparator.comparing(InstitutionBase::getName, Comparator.nullsLast(String::compareTo)))
                .toList();
        log.debug("getUserProducts result = {}", result);
        log.trace("getUserProducts end");
        return result;
    }

    @Override
    public UserInstitution getProducts(String institutionId, String userId) {
        log.trace("getProducts start");
        List<UserInstitutionResponse> institutionResponses = userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(
                institutionId,
                null,
                null,
                null,
                null,
                userId
        ).getBody();


        if (CollectionUtils.isEmpty(institutionResponses) || institutionResponses.size() != 1){
            throw new ResourceNotFoundException(String.format("InstitutionId %s and userId %s not found", institutionId, userId));
        }

        log.debug("getProducts result = {}", institutionResponses);
        log.trace("getProducts end");
        return institutionMapper.toInstitution(institutionResponses.get(0));
    }

    @Override
    public Boolean hasPermission(String institutionId, String permission, String productId) {
        log.trace("permissionInstitutionIdPermissionGet start");
        log.debug("permissionInstitutionIdPermissionGet institutionId = {}, permission = {}, productId = {}", institutionId, permission, productId);

        PermissionTypeEnum permissionTypeEnum = PermissionTypeEnum.fromValue(permission);
        Boolean result = userPermissionRestClient._authorizeInstitutionIdGet(institutionId, permissionTypeEnum, productId).getBody();

        log.debug("permissionInstitutionIdPermissionGet result = {}", result);
        log.trace("permissionInstitutionIdPermissionGet end");
        return result;
    }

    @Override
    public User getUserById(String userId, String institutionId, List<String> fields) {
        log.trace("getUserById start");
        log.debug("getUserById id = {}", userId);
        String fieldsString = !CollectionUtils.isEmpty(fields) ? String.join(",", fields) : null;
        User user = userMapper.toUser(userApiRestClient._usersIdDetailsGet(userId, fieldsString, institutionId).getBody());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserById = {}", user);
        log.trace("getUserById end");
        return user;
    }

    @Override
    public User searchByFiscalCode(String fiscalCode, String institutionId) {
        log.trace("searchByFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode fiscalCode = {}", fiscalCode);
        User user = userMapper.toUser(userApiRestClient._usersSearchPost(institutionId, SearchUserDto.builder().fiscalCode(fiscalCode).build()).getBody());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode user = {}", user);
        log.trace("searchByFiscalCode end");
        return user;
    }

    @Override
    public void suspendUserProduct(String userId, String institutionId, String productId) {
        log.trace("suspend start");
        log.debug("suspend userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.SUSPENDED);
        log.trace("suspend end");
    }

    @Override
    public void activateUserProduct(String userId, String institutionId, String productId) {
        log.trace("activate start");
        log.debug("activate userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.ACTIVE);
        log.trace("activate end");
    }

    @Override
    public void deleteUserProduct(String userId, String institutionId, String productId) {
        log.trace("delete start");
        log.debug("delete userId = {}, institutionId = {}", userId, institutionId);
        userApiRestClient._usersIdInstitutionInstitutionIdProductProductIdStatusPut(userId, institutionId, productId, OnboardedProductState.DELETED);
        log.trace("delete end");
    }

    @Override
    public void updateUser(String userId, String institutionId, UpdateUserRequestDto userDto) {
        log.trace("updateUser start");
        log.debug("updateUser userId = {}, institutionId = {}, userDto = {}", userId, institutionId, userDto);
        userApiRestClient._usersIdUserRegistryPut(userId, institutionId, userMapper.toUpdateUserRequest(userDto));
        log.trace("updateUser end");
    }

    @Override
    public Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter, String loggedUserId) {
        log.trace("getUsers start");
        log.debug("getUsers institutionId = {}, userInfoFilter = {}", institutionId, userInfoFilter);

        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);

        List<String> roles = Arrays.stream(PartyRole.values())
                .filter(partyRole -> partyRole.getSelfCareAuthority().equals(userInfoFilter.getRole()))
                .map(Enum::name)
                .toList();

        return Optional.ofNullable(userApiRestClient._usersUserIdInstitutionInstitutionIdGet(institutionId,
                                loggedUserId,
                                userInfoFilter.getUserId(),
                                userInfoFilter.getProductRoles(),
                                StringUtils.hasText(userInfoFilter.getProductId()) ? List.of(userInfoFilter.getProductId()) : null,
                                !CollectionUtils.isEmpty(roles) ? roles : null,
                                !CollectionUtils.isEmpty(userInfoFilter.getAllowedStates()) ? userInfoFilter.getAllowedStates().stream().map(Enum::name).toList() : null)
                        .getBody())
                .map(userDataResponses -> userDataResponses.stream()
                        .map(userMapper::toUserInfo)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public List<UserInstitution> retrieveFilteredUser(String userId, String institutionId, String productId) {
        log.trace("retrieveFilteredUser start");
        log.debug("retrieveFilteredUser userId = {}, institutionId = {}, productId = {}", userId, institutionId, productId);
        List<UserInstitutionResponse> institutionResponses = userInstitutionApiRestClient._institutionsInstitutionIdUserInstitutionsGet(institutionId, null, List.of(productId), null, getValidUserStates(), userId).getBody();
        if(!CollectionUtils.isEmpty(institutionResponses)) {
            log.info("retrieveFilteredUser institutionResponses size = {}", institutionResponses.size());
            return institutionResponses.stream()
                    .map(userMapper::toUserInstitution)
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<String> getValidUserStates() {
        return Stream.of(OnboardedProductState.values())
                .filter(onboardedProductState -> onboardedProductState != OnboardedProductState.DELETED && onboardedProductState != OnboardedProductState.REJECTED)
                .map(Enum::name)
                .toList();
    }

    @Override
    public String createOrUpdateUserByFiscalCode(Institution institution, String productId, UserToCreate userDto, List<it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role> roles) {
        log.trace("createOrUpdateUserByFiscalCode start");
        log.debug("createOrUpdateUserByFiscalCode userDto = {}", userDto);
        if(roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Role list cannot be empty");
        }

        CreateUserDto createUserDto = buildCreateUserDto(institution, productId, userDto, roles);
        String userId = userApiRestClient._usersPost(createUserDto).getBody();

        log.trace("createOrUpdateUserByFiscalCode end");
        return userId;
    }

    private CreateUserDto buildCreateUserDto(Institution institution, String productId, UserToCreate userDto, List<it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role> roles) {
        var builder = CreateUserDto.builder()
                .institutionId(institution.getId())
                .institutionDescription(institution.getDescription())
                .user(buildUser(userDto))
                .product(buildProduct(productId, roles));

        if (!Objects.isNull(institution.getRootParent())) {
            builder.institutionRootName(institution.getRootParent().getDescription());
        }

        return builder.build();
    }

    private it.pagopa.selfcare.user.generated.openapi.v1.dto.User buildUser(UserToCreate userDto) {
        return it.pagopa.selfcare.user.generated.openapi.v1.dto.User.builder()
                .fiscalCode(userDto.getTaxCode())
                .name(userDto.getName())
                .familyName(userDto.getSurname())
                .institutionEmail(userDto.getEmail())
                .build();
    }
    private Product1 buildProduct(String productId, List<it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role> roles) {
        return Product1.builder()
                .productRoles(roles.stream().map(it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role::getProductRole).toList())
                .role(it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole.valueOf(roles.get(0).getPartyRole().name()))
                .productId(productId)
                .build();
    }

    @Override
    public void createOrUpdateUserByUserId(Institution institution, String productId, String userId, List<it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role> roles) {
        log.trace("createOrUpdateUserByUserId start");

        var addUserRoleDtoBuilder = AddUserRoleDto.builder()
                .institutionId(institution.getId())
                .institutionDescription(institution.getDescription())
                .product(Product.builder()
                        .productRoles(roles.stream().map(it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto.Role::getProductRole).toList())
                        .role(it.pagopa.selfcare.user.generated.openapi.v1.dto.PartyRole.valueOf(roles.get(0).getPartyRole().name()))
                        .productId(productId)
                        .build());

        if(!Objects.isNull(institution.getRootParent())){
            addUserRoleDtoBuilder.institutionRootName(institution.getRootParent().getDescription());
        }

        AddUserRoleDto addUserRoleDto = addUserRoleDtoBuilder.build();

        userApiRestClient._usersUserIdPost(userId, addUserRoleDto);
        log.trace("createOrUpdateUserByUserId end");
    }

}
