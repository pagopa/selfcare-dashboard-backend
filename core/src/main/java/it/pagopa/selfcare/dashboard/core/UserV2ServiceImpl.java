package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.product.mapper.ProductMapper;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.exception.InvalidOnboardingStatusException;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;


@Slf4j
@Service
public class UserV2ServiceImpl implements UserV2Service {

    private final MsCoreConnector msCoreConnector;
    private final UserGroupV2Service userGroupService;
    private final UserApiConnector userApiConnector;
    private final ProductsConnector productsConnector;

    private final List<RelationshipState> allowedStates;

    public UserV2ServiceImpl(
            MsCoreConnector msCoreConnector,
            UserGroupV2Service userGroupService,
            UserApiConnector userApiConnector, ProductsConnector productsConnector,
            @Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates
    ) {
        this.msCoreConnector = msCoreConnector;
        this.userGroupService = userGroupService;
        this.userApiConnector = userApiConnector;
        this.productsConnector = productsConnector;
        this.allowedStates = allowedStates != null && allowedStates.length != 0 ? Arrays.stream(allowedStates).map(RelationshipState::valueOf).toList() : null;
    }


    @Override
    public Collection<InstitutionBase> getInstitutions(String userId) {
        log.trace("getInstitutions start");
        Collection<InstitutionBase> result = userApiConnector.getUserInstitutions(userId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }

    @Override
    public void deleteUserProduct(String userId, String institutionId, String productId, String productRole) {
        log.trace("delete start");
        log.debug("delete userId = {} for institutionId = {}, productId = {} and productRole = {}", userId, institutionId, productId, productRole);
        userApiConnector.deleteUserProduct(userId, institutionId, productId, productRole);
        userGroupService.deleteMembersByUserId(userId, institutionId, productId);
        log.trace("delete end");
    }

    @Override
    public void activateUserProduct(String userId, String institutionId, String productId, String productRole) {
        log.trace("activate start");
        log.debug("activate userId = {} for institutionId = {}, productId = {} and productRole = {}", userId, institutionId, productId, productRole);
        userApiConnector.activateUserProduct(userId, institutionId, productId, productRole);
        log.trace("activate end");

    }

    @Override
    public void suspendUserProduct(String userId, String institutionId, String productId, String productRole) {
        log.trace("suspend start");
        log.debug("suspend userId = {} for institutionId = {} and product = {}", userId, institutionId, productId);
        userApiConnector.suspendUserProduct(userId, institutionId, productId, productRole);
        log.trace("suspend end");
    }

    @Override
    public User getUserById(String userId, String institutionId, List<String> fields) {
        log.trace("getUserById start");
        log.debug("getUserById id = {}", userId);
        User user = userApiConnector.getUserById(userId, institutionId, fields);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getUserById = {}", user);
        log.trace("getUserById end");
        return user;
    }

    @Override
    public User searchUserByFiscalCode(String fiscalCode, String institutionId) {
        log.trace("searchByFiscalCode start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode fiscalCode = {}", fiscalCode);
        User user = userApiConnector.searchByFiscalCode(fiscalCode, institutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "searchByFiscalCode user = {}", user);
        log.trace("searchByFiscalCode end");
        return user;
    }

    @Override
    public void updateUser(String id, String institutionId, UpdateUserRequestDto userDto) {
        log.trace("updateUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "updateUser id = {}, institutionId = {}, userDto = {}", id, institutionId, userDto);
        Assert.notNull(id, "UUID is required");
        Assert.hasText(institutionId, "An institutionId is required");
        Assert.notNull(userDto, "A userDto is required");
        final Institution institution = msCoreConnector.getInstitution(institutionId);
        if (Objects.isNull(institution)) {
            throw new ResourceNotFoundException("There is no institution for given institutionId");
        }
        userApiConnector.updateUser(id, institutionId, userDto);
        log.trace("updateUser end");
    }

    @Override
    public Collection<UserInfo> getUsersByInstitutionId(String institutionId, String productId, List<String> productRoles, List<String> roles, String loggedUserId) {
        log.trace("getUsersByInstitutionId start");
        log.debug("getUsersByInstitutionId institutionId = {}", institutionId);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles);
        userInfoFilter.setAllowedStates(allowedStates);
        userInfoFilter.setRoles(roles);
        Collection<UserInfo> result = userApiConnector.getUsers(institutionId, userInfoFilter, loggedUserId);
        log.info("getUsersByInstitutionId result size = {}", result.size());
        log.trace("getUsersByInstitutionId end");
        return result;
    }

    @Override
    public String createUsers(String institutionId, String productId, UserToCreate userDto) {
        log.trace("createOrUpdateUserByFiscalCode start");
        log.debug("createOrUpdateUserByFiscalCode userDto = {}", userDto);
        Institution institution = verifyOnboardingStatus(institutionId, productId);
        List<CreateUserDto.Role> role = retrieveRole(productId, userDto.getProductRoles(), userDto.getRole());
        String userId = userApiConnector.createOrUpdateUserByFiscalCode(institution, productId, userDto, role);
        log.trace("createOrUpdateUserByFiscalCode end");
        return userId;
    }

    @Override
    public void addUserProductRoles(String institutionId, String productId, String userId, Set<String> productRoles, String role) {
        log.trace("createOrUpdateUserByUserId start");
        log.debug("createOrUpdateUserByUserId userId = {}", userId);
        Institution institution = verifyOnboardingStatus(institutionId, productId);
        PartyRole partyRole = Optional.ofNullable(role).map(PartyRole::valueOf).orElse(null);
        List<CreateUserDto.Role> roleDto = retrieveRole(productId, productRoles, partyRole);
        userApiConnector.createOrUpdateUserByUserId(institution, productId, userId, roleDto);
        log.trace("createOrUpdateUserByUserId end");
    }

    private Institution verifyOnboardingStatus(String institutionId, String productId) {
        Institution institution = msCoreConnector.getInstitution(institutionId);
        if (institution.getOnboarding() == null || institution.getOnboarding().stream()
                .noneMatch(onboarding -> onboarding.getProductId().equals(productId) && onboarding.getStatus().equals(RelationshipState.ACTIVE))
        ) {
            throw new InvalidOnboardingStatusException("The product is not active for the institution");
        }
        else return institution;
    }

    /**
     * This method is used to retrieve a list of roles for a given product.
     * It maps each product role to a CreateUserDto.Role object, which includes the label and party role.
     * To retrieve the party role, it uses the roleMappings of the product filtering by a white list of party roles (Only SUB_DELEGATE and OPERATOR are allowed
     * as Role to be assigned to a user in add Users ProductRoles operation).
     */
    private List<CreateUserDto.Role> retrieveRole(String productId, Set<String> productRoles, PartyRole partyRole) {
        Product product = productsConnector.getProduct(productId);
        return productRoles.stream().map(productRole -> {
            CreateUserDto.Role role = new CreateUserDto.Role();
            role.setProductRole(productRole);
            role.setLabel(ProductMapper.getLabel(productRole, product.getRoleMappings(null)).orElse(null));
            role.setPartyRole(partyRole);
            return role;
        }).toList();
    }

}
