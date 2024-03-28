package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;

@Slf4j
@Service
class InstitutionV2ServiceImpl implements InstitutionV2Service {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    private static final String REQUIRED_USER_ID = "A user id is required";
    private static final String A_USER_INFO_FILTER_OBJECT_IS_REQUIRED = "A UserInfoFilter object is required";

    private final List<RelationshipState> allowedStates;
    private final UserApiConnector userApiConnector;
    private final MsCoreConnector msCoreConnector;

    @Autowired
    public InstitutionV2ServiceImpl(@Value("${dashboard.institution.getUsers.filter.states}") String[] allowedStates,
                                    UserApiConnector userApiConnector,
                                  MsCoreConnector msCoreConnector) {
        this.allowedStates = allowedStates != null && allowedStates.length != 0 ? Arrays.stream(allowedStates).map(RelationshipState::valueOf).toList() : null;
        this.userApiConnector = userApiConnector;
        this.msCoreConnector = msCoreConnector;
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
        UserInstitution userInstitution = userApiConnector.getProducts(institutionId, userId);
        Institution institution = msCoreConnector.getInstitution(institutionId);

        if (userInstitution != null) {
            boolean limited = userInstitution.getProducts().stream().noneMatch(prod -> SelfCareAuthority.ADMIN.equals(prod.getRole().getSelfCareAuthority()));
            if (limited) {
                institution.getOnboarding().stream()
                        .filter(product -> userInstitution.getProducts().stream().anyMatch(prodUser -> product.getProductId().equals(prodUser.getProductId())))
                        .forEach(product -> {product.setAuthorized(true); product.setUserRole(LIMITED.name());});
            } else {
                institution.getOnboarding().forEach(product -> {
                    product.setAuthorized(userInstitution.getProducts().stream().anyMatch(prodUser -> product.getProductId().equals(prodUser.getProductId())));
                    userInstitution.getProducts().stream().filter(prodUser -> product.getProductId().equals(prodUser.getProductId())).findAny().ifPresentOrElse(userProd -> product.setUserRole(userProd.getRole().name()), () -> product.setUserRole(null));
                });
            }
        }
        log.debug("findInstitutionById result = {}", institution);
        log.trace("findInstitutionById end");
        return institution;
    }

}
