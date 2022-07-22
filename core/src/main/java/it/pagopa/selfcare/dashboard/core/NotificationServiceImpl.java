package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailPreparationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final String ACTIVATE_SUBJECT = "User has been activated";
    private static final String DELETE_SUBJECT = "User had been deleted";
    private static final String SUSPEND_SUBJECT = "User has been suspended";
    private static final String CREATE_SUBJECT = "A new user has been added";
    private static final String ACTIVATE_TEMPLATE = "user_activated.ftlh";
    private static final String DELETE_TEMPLATE = "user_deleted.ftlh";
    private static final String SUSPEND_TEMPLATE = "user_suspended.ftlh";
    private static final String CREATE_TEMPLATE_SINGLE_ROLE = "user_added_single_role.ftlh";
    private static final String CREATE_TEMPLATE_MULTIPLE_ROLE = "user_added_multi_role.ftlh";
    private static final EnumSet<User.Fields> EMAIL_FIELD_LIST = EnumSet.of(User.Fields.workContacts);
    private static final String A_PRODUCT_TITLE_IS_REQUIRED = "A product Title is required";
    private static final String INSTITUTION_ID_IS_REQUIRED = "An institution id is required";
    private static final String PRODUCT_ROLES_ARE_REQUIRED = "ProductRoles are required";

    private final Configuration freemarkerConfig;
    private final NotificationServiceConnector notificationConnector;
    private final ProductsConnector productsConnector;
    private final PartyConnector partyConnector;
    private final UserService userService;
    private final UserRegistryConnector userConnector;


    @Autowired
    public NotificationServiceImpl(Configuration freemarkerConfig,
                                   NotificationServiceConnector notificationConnector,
                                   ProductsConnector productsConnector,
                                   PartyConnector partyConnector, UserService userService, UserRegistryConnector userConnector) {
        this.freemarkerConfig = freemarkerConfig;
        this.notificationConnector = notificationConnector;
        this.productsConnector = productsConnector;
        this.partyConnector = partyConnector;
        this.userService = userService;
        this.userConnector = userConnector;
    }


    @Override
    @Async
    public void sendAddedProductRoleNotification(String institutionId, String productTitle, String userId, Set<CreateUserDto.Role> productRoles) {
        log.trace("sendAddedProductRoleNotification start");
        log.debug("sendAddedProductRoleNotification institutionId = {}, productTitle = {}, userId = {}, productRoles = {}", institutionId, productTitle, userId, productRoles);
        Assert.notNull(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.notNull(productTitle, A_PRODUCT_TITLE_IS_REQUIRED);
        Assert.notEmpty(productRoles, PRODUCT_ROLES_ARE_REQUIRED);
        User user = userConnector.getUserByInternalId(userId, EMAIL_FIELD_LIST);
        Optional<String> email = Optional.empty();
        if (null != user.getWorkContact(institutionId)) {
            email = Optional.ofNullable(user.getWorkContact(institutionId).getEmail().getValue());
        }
        Assert.isTrue(email.isPresent(), "User workContact is required");

        sendCreateNotification(institutionId, productTitle, email.get(), productRoles);
        log.trace("sendAddedProductRoleNotification start");
    }

    @Override
    @Async
    public void sendCreatedUserNotification(String institutionId, String productTitle, String email, Set<CreateUserDto.Role> productRoles) {
        log.debug("sendCreatedUserNotification start");
        log.debug("sendCreatedUserNotification institutionId = {}, productTitle = {}, email = {}, productRoles = {}", institutionId, productTitle, email, productRoles);

        Assert.notNull(institutionId, INSTITUTION_ID_IS_REQUIRED);
        Assert.notNull(email, "User email is required");
        Assert.notNull(productTitle, A_PRODUCT_TITLE_IS_REQUIRED);
        Assert.notEmpty(productRoles, PRODUCT_ROLES_ARE_REQUIRED);
        sendCreateNotification(institutionId, productTitle, email, productRoles);
        log.debug("sendCreatedUserNotification end");
    }

    private void sendCreateNotification(String institutionId, String productTitle, String email, Set<CreateUserDto.Role> productRoles) {
        log.debug("sendCreateNotification start");
        log.debug("sendCreateNotification institutionId = {}, productTitle = {}, email = {}", institutionId, productTitle, email);

        Institution institution = partyConnector.getInstitution(institutionId);
        Assert.notNull(institution.getDescription(), "An institution description is required");
        List<String> roleLabels = productRoles.stream()
                .map(CreateUserDto.Role::getLabel)
                .collect(Collectors.toList());
        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("productName", productTitle);
        dataModel.put("institutionName", institution.getDescription());
        if (roleLabels.size() > 1) {
            String roleLabel = roleLabels.stream()
                    .limit(productRoles.size() - 1)
                    .collect(Collectors.joining(", "));

            dataModel.put("productRoles", roleLabel);
            dataModel.put("lastProductRole", roleLabels.get(roleLabels.size() - 1));
            sendNotification(email, CREATE_TEMPLATE_MULTIPLE_ROLE, CREATE_SUBJECT, dataModel);
        } else {
            String roleLabel = roleLabels.get(0);
            dataModel.put("productRole", roleLabel);
            sendNotification(email, CREATE_TEMPLATE_SINGLE_ROLE, CREATE_SUBJECT, dataModel);
        }
        log.debug("sendCreateNotification end");
    }


    private void sendNotification(String email, String templateName, String subject, Map<String, String> dataModel) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser principal = ((SelfCareUser) authentication.getPrincipal());
        dataModel.put("requesterName", principal.getUserName());
        dataModel.put("requesterSurname", principal.getSurname());

        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, dataModel);
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setContent(html);
            messageRequest.setReceiverEmail(email);
            messageRequest.setSubject(subject);
            notificationConnector.sendNotificationToUser(messageRequest);
        } catch (Exception e) {
            throw new MailPreparationException(e);
        }
    }


    @Override
    @Async
    public void sendActivatedUserNotification(String relationshipId) {
        log.trace("sendActivatedUserNotification start");
        log.debug("sendActivatedUserNotification relationshipId = {}", relationshipId);
        sendRelationshipBasedNotification(relationshipId, ACTIVATE_TEMPLATE, ACTIVATE_SUBJECT);
        log.debug("sendActivatedUserNotification end");
    }


    @Override
    @Async
    public void sendDeletedUserNotification(String relationshipId) {
        log.trace("sendDeletedUserNotification start");
        log.debug("sendDeletedUserNotification relationshipId = {}", relationshipId);
        sendRelationshipBasedNotification(relationshipId, DELETE_TEMPLATE, DELETE_SUBJECT);
        log.debug("sendDeletedUserNotification end");
    }


    @Override
    @Async
    public void sendSuspendedUserNotification(String relationshipId) {
        log.trace("sendSuspendedUserNotification start");
        log.debug("sendSuspendedUserNotification relationshipId = {}", relationshipId);
        sendRelationshipBasedNotification(relationshipId, SUSPEND_TEMPLATE, SUSPEND_SUBJECT);
        log.debug("sendSuspendedUserNotification end");
    }


    private void sendRelationshipBasedNotification(String relationshipId, String templateName, String subject) {
        Assert.notNull(relationshipId, "A relationship Id is required");
        UserInfo user = userService.findByRelationshipId(relationshipId, EnumSet.of(User.Fields.workContacts));
        Assert.notNull(user.getInstitutionId(), INSTITUTION_ID_IS_REQUIRED);
        Optional<String> email = Optional.empty();
        if (null != user.getUser().getWorkContact(user.getInstitutionId())) {
            email = Optional.ofNullable(user.getUser().getWorkContact(user.getInstitutionId()).getEmail().getValue());
        }
        Assert.isTrue(email.isPresent(), "User workContact is required");

        ProductInfo productInfo = user.getProducts().values().iterator().next();
        Assert.notNull(productInfo.getId(), "A product Id is required");
        Institution institution = partyConnector.getInstitution(user.getInstitutionId());
        Assert.notNull(institution.getDescription(), "An institution description is required");
        Product product = productsConnector.getProduct(productInfo.getId());
        Assert.notNull(product.getTitle(), A_PRODUCT_TITLE_IS_REQUIRED);
        Optional<String> roleLabel = product.getRoleMappings().values().stream()
                .flatMap(productRoleInfo -> productRoleInfo.getRoles().stream())
                .filter(productRole -> productRole.getCode().equals(productInfo.getRoleInfos().get(0).getRole()))
                .map(ProductRoleInfo.ProductRole::getLabel)
                .findAny();

        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("productName", product.getTitle());
        dataModel.put("productRole", roleLabel.orElse("no_role_found"));
        dataModel.put("institutionName", institution.getDescription());

        sendNotification(email.get(), templateName, subject, dataModel);
    }

}
