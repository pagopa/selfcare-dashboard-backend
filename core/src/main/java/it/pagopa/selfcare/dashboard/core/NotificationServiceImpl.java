package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.RelationshipInfoResult;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.core.exception.TemplateProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailPreparationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private Configuration freemarkerConfig;
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
    public void sendNotificationRelationshipEvent(String relationshipId, String templateId) {
        log.trace("sendNotificationRelationshipEvent start");
        log.debug("sendNotificationRelationshipEvent relationshipId = {}", relationshipId);
        Assert.notNull(relationshipId, "A relationship Id is required");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser principal = ((SelfCareUser) authentication.getPrincipal());
        RelationshipInfoResult relationshipInfoResult = partyConnector.getRelationshipInfo(relationshipId);
        String email = relationshipInfoResult.getEmail();
        Product product = productsConnector.getProduct(relationshipInfoResult.getProductId());
        Optional<String> roleLabel = product.getRoleMappings().values().stream()
                .flatMap(productRoleInfo -> productRoleInfo.getRoles().stream())
                .filter(productRole -> productRole.getCode().equals(relationshipInfoResult.getProductRole()))
                .map(ProductRoleInfo.ProductRole::getLabel)
                .findAny();

        Map<String, String> dataModel = new HashMap<>();
        dataModel.put("productName", product.getTitle());
        dataModel.put("productRole", roleLabel.orElse("no_role_found"));
        dataModel.put("requesterName", principal.getUserName());
        dataModel.put("requesterSurname", principal.getSurname());

        try {
            Template template = freemarkerConfig.getTemplate(templateId);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, dataModel);
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setContent(html);
            messageRequest.setReceiverEmail(email);
            messageRequest.setSubject("User had been deleted");
            notificationConnector.sendNotificationToUser(messageRequest);
        } catch (TemplateException | IOException e) {
            throw new TemplateProcessingException("Error in processing the template to string");
        }

        log.trace("sendNotificationDeleteUserRelationship end");
    }


    @Override
    @Async
    public void sendNotificationCreateUserRelationship(String productTitle, String email) {
        log.trace("sendNotificationCreateUserRelationship start");
        log.debug("productTitle = {}, email = {}", productTitle, email);
        log.trace("sendNotificationCreateUserRelationship thread = {}", Thread.currentThread().getName());
        Map<String, String> dataModel = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.notNull(email, "User email is required");
        Assert.notNull(productTitle, "A product Title is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser principal = ((SelfCareUser) authentication.getPrincipal());

        dataModel.put("requesterName", principal.getUserName());
        dataModel.put("requesterSurname", principal.getSurname());
        dataModel.put("productName", productTitle);
        try {
            Template template = freemarkerConfig.getTemplate("add_referent.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, dataModel);
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setContent(html);
            messageRequest.setReceiverEmail(email);
            messageRequest.setSubject("User had been added");
            notificationConnector.sendNotificationToUser(messageRequest);

        } catch (TemplateException | IOException e) {
            //TODO MailPreparationException
            throw new MailPreparationException(e);
        }

        log.trace("sendNotificationCreateUserRelationship end");
    }

}
