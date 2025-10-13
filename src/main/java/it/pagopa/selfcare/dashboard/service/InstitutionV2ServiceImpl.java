package it.pagopa.selfcare.dashboard.service;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingResponse;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.OnboardingsResponse;
import it.pagopa.selfcare.dashboard.client.CoreInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.client.OnboardingRestClient;
import it.pagopa.selfcare.dashboard.client.TokenRestClient;
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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.PENDING;
import static it.pagopa.selfcare.onboarding.common.OnboardingStatus.TOBEVALIDATED;

@Slf4j
@Service
public class InstitutionV2ServiceImpl implements InstitutionV2Service {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private static final String REQUIRED_USER_ID = "A user id is required";
    private static final String A_USER_INFO_FILTER_OBJECT_IS_REQUIRED = "A UserInfoFilter object is required";
    public static final String ISSUER_PAGOPA = "PAGOPA";

    private final List<RelationshipState> allowedStates;
    private final UserApiRestClient userApiRestClient;
    private final CoreInstitutionApiRestClient coreInstitutionApiRestClient;
    private final OnboardingRestClient onboardingRestClient;
    private final TokenRestClient tokenRestClient;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final UserV2ServiceImpl userV2Service;

    @Autowired
    public InstitutionV2ServiceImpl(@Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates,
                                    UserApiRestClient userApiRestClient,
                                    CoreInstitutionApiRestClient coreInstitutionApiRestClient,
                                    OnboardingRestClient onboardingRestClient,
                                    TokenRestClient tokenRestClient,
                                    UserMapper userMapper,
                                    InstitutionMapper institutionMapper,
                                    UserV2ServiceImpl userV2Service) {
        this.allowedStates = allowedStates != null && allowedStates.length != 0 ? Arrays.stream(allowedStates).map(RelationshipState::valueOf).toList() : null;
        this.userApiRestClient = userApiRestClient;
        this.coreInstitutionApiRestClient = coreInstitutionApiRestClient;
        this.onboardingRestClient = onboardingRestClient;
        this.tokenRestClient = tokenRestClient;
        this.userMapper = userMapper;
        this.institutionMapper = institutionMapper;
        this.userV2Service = userV2Service;
    }

    @Override
    public UserInfo getInstitutionUser(String institutionId, String userId, String loggedUserId) {
        log.trace("getInstitutionUser start");
        log.debug("getInstitutionUser institutionId = {}, userId = {}", Encode.forJava(institutionId), Encode.forJava(userId));

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
                Encode.forJava(institutionId), userInfoFilter.getProductId(), userInfoFilter.getRole(), userInfoFilter.getProductRoles());
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(userInfoFilter, A_USER_INFO_FILTER_OBJECT_IS_REQUIRED);

        return userV2Service.getUsers(institutionId, userInfoFilter, loggedUserId)
                .stream()
                .findFirst();
    }

    @Override
    public Institution findInstitutionById(String institutionId) {
        log.trace("findInstitutionById start");
        log.debug("findInstitutionById institutionId = {}", Encode.forJava(institutionId));
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", Encode.forJava(institutionId));
        Institution institution = institutionMapper.toInstitution(coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId, null).getBody());
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        log.trace("getUserInstitutionWithActions start");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SelfCareUser selfCareUser = (SelfCareUser) authentication.getPrincipal();
        String issuer = selfCareUser.getIssuer();

        if (ISSUER_PAGOPA.equalsIgnoreCase(issuer)) {
            log.debug("Issuer is PAGOPA, skipping user-institution permission checks");
            return institution;
        }

        String userId = selfCareUser.getId();

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

    @Override
    public OnboardingsResponse getOnboardingsInfoResponse(String institutionId, List<String> products) {
        log.trace("getOnboardingsResponse start");

        OnboardingsResponse onboardingsResponse = Optional.ofNullable(
                coreInstitutionApiRestClient._getOnboardingsInstitutionUsingGET(institutionId, null).getBody()
        ).orElse(new OnboardingsResponse());

        List<OnboardingResponse> filteredOnboardings = Optional.ofNullable(products)
                .filter(prods -> !prods.isEmpty())
                .map(prods -> onboardingsResponse.getOnboardings().stream()
                        .filter(onboarding -> prods.contains(onboarding.getProductId()) && OnboardingResponse.StatusEnum.ACTIVE.equals(onboarding.getStatus()))
                        .toList())
                .orElse(onboardingsResponse.getOnboardings().stream()
                        .filter(onboarding -> OnboardingResponse.StatusEnum.ACTIVE.equals(onboarding.getStatus()))
                        .toList());

        OnboardingsResponse filteredResponse = new OnboardingsResponse();
        filteredResponse.setOnboardings(filteredOnboardings);

        log.trace("getOnboardingsResponse end");
        return filteredResponse;
    }

    @Override
    public Resource getContract(String institutionId, String productId) {
        OnboardingsResponse onboardingsResponse = Optional.ofNullable(
                coreInstitutionApiRestClient._getOnboardingsInstitutionUsingGET(institutionId, productId).getBody()
        ).orElseGet(() -> {
            OnboardingsResponse emptyResponse = new OnboardingsResponse();
            emptyResponse.setOnboardings(Collections.emptyList());
            return emptyResponse;
        });

        OnboardingResponse onboarding = Optional.ofNullable(onboardingsResponse.getOnboardings())
                .orElse(Collections.emptyList()).stream()
                .filter(onb -> OnboardingResponse.StatusEnum.ACTIVE.equals(onb.getStatus()))
                .max(Comparator.comparing(OnboardingResponse::getCreatedAt))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active onboarding found for institution " + institutionId + " and product " + productId
                ));

        return tokenRestClient._getContractSigned(onboarding.getTokenId()).getBody();
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
        ResponseEntity<OnboardingGetResponse> response = onboardingRestClient._getOnboardingWithFilter(null, null, null, null, productId, null,1, null, status, subunitCode, taxCode, null, null);
        return checkOnboardingPresence(response);
    }

    private static boolean checkOnboardingPresence(ResponseEntity<OnboardingGetResponse> response) {
        return Optional.ofNullable(response)
                .map(ResponseEntity::getBody)
                .map(OnboardingGetResponse::getItems)
                .filter(items -> !items.isEmpty())
                .isPresent();
    }
}
