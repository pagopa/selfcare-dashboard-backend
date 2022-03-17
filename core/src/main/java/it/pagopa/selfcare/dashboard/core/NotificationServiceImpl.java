package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final String ACTIVATE_SUBJECT = "User has been activated";
    private static final String DELETE_SUBJECT = "User had been deleted";
    private static final String SUSPEND_SUBJECT = "User has been suspended";
    private static final String CREATE_SUBJECT = "A new user has been added";
    private static final String ACTIVATE_TEMPLATE = "activate_referent.ftlh";
    private static final String DELETE_TEMPLATE = "delete_referent.ftlh";
    private static final String SUSPEND_TEMPLATE = "suspend_referent.ftlh";
    private static final String CREATE_TEMPLATE = "add_referent.ftlh";

    private final Configuration freemarkerConfig;
    private final NotificationServiceConnector notificationConnector;
    private final ProductsConnector productsConnector;
    private final PartyConnector partyConnector;


    @Autowired
    public NotificationServiceImpl(Configuration freemarkerConfig,
                                   NotificationServiceConnector notificationConnector,
                                   ProductsConnector productsConnector,
                                   PartyConnector partyConnector) {
        this.freemarkerConfig = freemarkerConfig;
        this.notificationConnector = notificationConnector;
        this.productsConnector = productsConnector;
        this.partyConnector = partyConnector;
    }


    @Override
    @Async
    public void sendCreatedUserNotification(String institutionExternalId, String productTitle, String email) {
        log.debug("sendCreatedUserNotification start");
        log.debug("institutionExternalId = {}, productTitle = {}, email = {}", institutionExternalId, productTitle, email);
        Assert.notNull(institutionExternalId, "Institution external id is required");
        Assert.notNull(email, "User email is required");
        Assert.notNull(productTitle, "A product Title is required");
        Institution institution = partyConnector.getInstitutionByExternalId(institutionExternalId);
        Assert.notNull(institution.getDescription(), "An institution description is required");

        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("productName", productTitle);
        dataModel.put("institutionName", institution.getDescription());

        sendNotification(email, CREATE_TEMPLATE, CREATE_SUBJECT, dataModel);
        log.debug("sendCreatedUserNotification end");
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
        UserInfo user = partyConnector.getUser(relationshipId);
        Assert.notNull(user.getEmail(), "User email is required");
        Assert.notNull(user.getInstitutionId(), "An institution id is required");
        ProductInfo productInfo = user.getProducts().values().iterator().next();
        Assert.notNull(productInfo.getId(), "A product Id is required");
        Institution institution = partyConnector.getInstitution(user.getInstitutionId());
        Assert.notNull(institution.getDescription(), "An institution description is required");
        Product product = productsConnector.getProduct(productInfo.getId());
        Assert.notNull(product.getTitle(), "A product Title is required");
        Optional<String> roleLabel = product.getRoleMappings().values().stream()
                .flatMap(productRoleInfo -> productRoleInfo.getRoles().stream())
                .filter(productRole -> productRole.getCode().equals(productInfo.getRoleInfos().get(0).getRole()))
                .map(ProductRoleInfo.ProductRole::getLabel)
                .findAny();

        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("productName", product.getTitle());
        dataModel.put("productRole", roleLabel.orElse("no_role_found"));
        dataModel.put("institutionName", institution.getDescription());

        sendNotification(user.getEmail(), templateName, subject, dataModel);
    }

}