package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.dashboard.connector.api.NotificationServiceConnector;
import it.pagopa.selfcare.dashboard.connector.model.notification.MessageRequest;
import it.pagopa.selfcare.dashboard.core.exception.TemplateProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private Configuration freemarkerConfig;
    private NotificationServiceConnector notificationConnector;


    @Autowired
    public NotificationServiceImpl(Configuration freemarkerConfig, NotificationServiceConnector notificationConnector) {
        this.freemarkerConfig = freemarkerConfig;
        this.notificationConnector = notificationConnector;
    }


    @Override
    public void sendNotificationCreateUserRelationship(String productTitle, String email) {
        log.trace("sendNotificationCreateUserRelationship start");
        System.out.println("notification core entered");
        Map<String, String> dataModel = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.notNull(email, "User email is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUSer principal");
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
            throw new TemplateProcessingException("Error in processing the template to string");
        }

        log.trace("sendNotificationCreateUserRelationship end");
    }
}
