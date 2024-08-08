package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.OnboardingConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.OnboardedProductWithActions;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitutionWithActionsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.PENDING;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.TOBEVALIDATED;

@Slf4j
@Service
class InstitutionV2ServiceImpl implements InstitutionV2Service {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private static final String REQUIRED_USER_ID = "A user id is required";
    private static final String A_USER_INFO_FILTER_OBJECT_IS_REQUIRED = "A UserInfoFilter object is required";

    private final List<RelationshipState> allowedStates;
    private final UserApiConnector userApiConnector;
    private final MsCoreConnector msCoreConnector;
    private final OnboardingConnector onboardingConnector;

    @Autowired
    public InstitutionV2ServiceImpl(@Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates,
                                    UserApiConnector userApiConnector,
                                    MsCoreConnector msCoreConnector,
                                    OnboardingConnector onboardingConnector) {
        this.allowedStates = allowedStates != null && allowedStates.length != 0 ? Arrays.stream(allowedStates).map(RelationshipState::valueOf).toList() : null;
        this.userApiConnector = userApiConnector;
        this.msCoreConnector = msCoreConnector;
        this.onboardingConnector = onboardingConnector;
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

        return userApiConnector.getUsers(institutionId, userInfoFilter, loggedUserId)
                .stream()
                .findFirst();
    }

    @Override
    public Institution findInstitutionById(String institutionId) {
        log.trace("findInstitutionById start");
        log.debug("findInstitutionById institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        String userId = ((SelfCareUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        Institution institution = msCoreConnector.getInstitution(institutionId);
        UserInstitutionWithActionsDto userInstitutionWithActionsDto = userApiConnector.getUserInstitutionWithActions(institutionId, userId, null);

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
        Boolean response = onboardingConnector.getOnboardingWithFilter(taxCode, subunitCode,  productId, PENDING.name());
        if (Boolean.FALSE.equals(response)) {
            response = onboardingConnector.getOnboardingWithFilter(taxCode, subunitCode, productId, TOBEVALIDATED.name());
        }
        return response;
    }
}
