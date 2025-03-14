package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.client.CoreInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.client.OnboardingRestClient;
import it.pagopa.selfcare.dashboard.client.UserApiRestClient;
import it.pagopa.selfcare.dashboard.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.model.institution.Institution;
import it.pagopa.selfcare.dashboard.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.model.mapper.InstitutionMapper;
import it.pagopa.selfcare.dashboard.model.mapper.UserMapper;
import it.pagopa.selfcare.dashboard.model.user.OnboardedProductWithActions;
import it.pagopa.selfcare.dashboard.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.model.user.UserInstitutionWithActionsDto;
import it.pagopa.selfcare.onboarding.generated.openapi.v1.dto.OnboardingGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.PENDING;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.TOBEVALIDATED;

@Slf4j
@Service
public class InstitutionV2ServiceImpl implements InstitutionV2Service {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private static final String REQUIRED_USER_ID = "A user id is required";
    private static final String A_USER_INFO_FILTER_OBJECT_IS_REQUIRED = "A UserInfoFilter object is required";

    private final List<RelationshipState> allowedStates;
    private final UserApiRestClient userApiRestClient;
    private final CoreInstitutionApiRestClient coreInstitutionApiRestClient;
    private final OnboardingRestClient onboardingRestClient;

    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;

    @Autowired
    public InstitutionV2ServiceImpl(@Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates,
                                    UserApiRestClient userApiRestClient,
                                    CoreInstitutionApiRestClient coreInstitutionApiRestClient,
                                    OnboardingRestClient onboardingRestClient,
                                    UserMapper userMapper, InstitutionMapper institutionMapper) {
        this.allowedStates = allowedStates != null && allowedStates.length != 0 ? Arrays.stream(allowedStates).map(RelationshipState::valueOf).toList() : null;
        this.userApiRestClient = userApiRestClient;
        this.coreInstitutionApiRestClient = coreInstitutionApiRestClient;
        this.onboardingRestClient = onboardingRestClient;
        this.userMapper = userMapper;
        this.institutionMapper = institutionMapper;
    }

    @Override
    public UserInfo getInstitutionUser(String institutionId, String userId, String loggedUserId) {
        log.trace("getInstitutionUser start");
        log.debug("getInstitutionUser institutionId = {}, userId = {}", institutionId, userId);

        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.hasText(userId, REQUIRED_USER_ID);
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        userInfoFilter.setAllowedStates(allowedStates);

        return getUserInfo(institutionId, userInfoFilter, loggedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No User found for the given userId"));
    }

    private Optional<UserInfo> getUserInfo(String institutionId, UserInfo.UserInfoFilter userInfoFilter, String loggedUserId) {
        log.trace("getInstitutionUsers start");
        log.debug("getInstitutionUsers institutionId = {}, productId = {}, role = {}, productRoles = {}",
                institutionId, userInfoFilter.getProductId(), userInfoFilter.getRole(), userInfoFilter.getProductRoles());
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(userInfoFilter, A_USER_INFO_FILTER_OBJECT_IS_REQUIRED);

        return getUsers(institutionId, userInfoFilter, loggedUserId)
                .stream()
                .findFirst();
    }

    private Collection<UserInfo> getUsers(String institutionId, UserInfo.UserInfoFilter userInfoFilter, String loggedUserId) {
        log.trace("getUsers start");
        log.debug("getUsers institutionId = {}, userInfoFilter = {}", Encode.forJava(institutionId), userInfoFilter);

        List<String> roles = Arrays.stream(PartyRole.values())
                .filter(partyRole -> partyRole.getSelfCareAuthority().equals(userInfoFilter.getRole()))
                .map(Enum::name)
                .toList();

        return Optional.ofNullable(userApiRestClient._retrieveUsers(institutionId,
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
    public Institution findInstitutionById(String institutionId) {
        log.trace("findInstitutionById start");
        log.debug("findInstitutionById institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        String userId = ((SelfCareUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", Encode.forJava(institutionId));
        Institution institution = institutionMapper.toInstitution(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId).getBody());
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        log.trace("getUserInstitutionWithActions start");
        UserInstitutionWithActionsDto userInstitutionWithActionsDto = userMapper.toUserInstitutionWithActionsDto(userApiRestClient._getUserInstitutionWithPermission(institutionId, userId, null).getBody());

        if (Objects.isNull(userInstitutionWithActionsDto))
            throw new AccessDeniedException(String.format("User %s has not associations with institution %s", userId, institutionId));

        if (Objects.isNull(institution) || Objects.isNull(institution.getOnboarding()))
            throw new ResourceNotFoundException(String.format("Institution %s not found or onboarding is empty!", institutionId));

        institution.getOnboarding().stream()
                .filter(onboardedProduct -> RelationshipState.ACTIVE.equals(onboardedProduct.getStatus()))
                .filter(product -> userInstitutionWithActionsDto.getProducts().stream().anyMatch(prodUser -> product.getProductId().equals(prodUser.getProductId())))
                .forEach(product -> {
                    var onBoardedProductWithActions = getOnBoardedProductWithActions(product.getProductId(), userInstitutionWithActionsDto);
                    product.setAuthorized(userInstitutionWithActionsDto.getProducts().stream().anyMatch(prodUser -> product.getProductId().equals(prodUser.getProductId())));
                    product.setUserRole(onBoardedProductWithActions.getRole().getSelfCareAuthority().name());
                    product.setUserProductActions(onBoardedProductWithActions.getUserProductActions());
                });

        log.debug("findInstitutionById result = {}", institution);
        log.trace("findInstitutionById end");
        return institution;
    }

    private OnboardedProductWithActions getOnBoardedProductWithActions(String productId, UserInstitutionWithActionsDto userInstitutionWithActionsDto) {
        return userInstitutionWithActionsDto.getProducts().stream().filter(product -> product.getProductId().equals(productId)).findFirst().orElse(null);
    }

    @Override
    public Boolean verifyIfExistsPendingOnboarding(String taxCode, String subunitCode, String productId) {
        Boolean response = getOnboardingWithFilter(taxCode, subunitCode, productId, PENDING.name());
        if (Boolean.FALSE.equals(response)) {
            response = getOnboardingWithFilter(taxCode, subunitCode, productId, TOBEVALIDATED.name());
        }
        return response;
    }

    private Boolean getOnboardingWithFilter(String taxCode, String subunitCode, String productId, String status) {
        ResponseEntity<OnboardingGetResponse> response = onboardingRestClient._getOnboardingWithFilter(null, null, null, null, productId, 1, status, subunitCode, taxCode, null);
        return checkOnboardingPresence(response);
    }

    private static boolean checkOnboardingPresence(ResponseEntity<OnboardingGetResponse> response) {
        return Objects.nonNull(response)
                && Objects.nonNull(response.getBody())
                && !CollectionUtils.isEmpty(response.getBody().getItems());
    }
}
