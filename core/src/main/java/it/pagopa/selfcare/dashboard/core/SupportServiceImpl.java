package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Template;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;


import java.util.*;


@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

    private final String supportApiKey;
    private final String zendeskOrganization;
    private final String returnTo;
    private final String redirectUrl;
    @Qualifier("zendeskFreeMarker")
    private final FreeMarkerConfigurer freeMarkerConfigurer;

    public SupportServiceImpl(@Value("${support.api.key}") String supportApiKey,
                              @Value("${support.api.zendesk.redirectUri}") String returnTo,
                              @Value("${support.api.zendesk.organization}") String zendeskOrganization,
                              @Value("${support.api.zendesk.actionUri}") String redirectUrl,
                              FreeMarkerConfigurer freeMarkerConfigurer) {
        this.supportApiKey = supportApiKey;
        this.returnTo = returnTo;
        this.zendeskOrganization = zendeskOrganization;
        this.redirectUrl = redirectUrl;
        this.freeMarkerConfigurer = freeMarkerConfigurer;
    }

    @Override
    public String sendRequest(SupportRequest supportRequest) {

        log.trace("sendRequest start");
        log.debug("sendRequest request = {}", supportRequest);
        String jwtString;
        try {
            jwtString = Jwts.builder()
                    .setIssuedAt(new Date())
                    .setId(UUID.randomUUID().toString())
                    .claim("name", supportRequest.getName())
                    .claim("email", supportRequest.getEmail())
                    .claim("organization", zendeskOrganization)
                    .claim("user_fields", supportRequest.getUserFields())
                    .signWith(SignatureAlgorithm.HS256, supportApiKey.getBytes())
                    .compact();
        } catch(Exception e) {
            log.error("Impossible to sign zendesk jwt. Error: {}", e.getMessage(), e);
            throw new SupportException(e.getMessage());
        }

        String redirectToProduct = Objects.nonNull(supportRequest.getProductId()) ?
                returnTo.concat("?product=" + supportRequest.getProductId()) : returnTo;

        Map<String, String> map = new HashMap<>();
        map.put("jwt", jwtString);
        map.put("returnTo", redirectToProduct);
        map.put("action", redirectUrl);

        String html;

        try {
            Template freemarkerTemplate = freeMarkerConfigurer.getConfiguration()
                    .getTemplate("/template-zendesk-form.ftl");
            html = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, map);
        } catch (Exception e){
            String errorMessage = "Impossible to retrieve zendesk form template";
            log.error(errorMessage);
            throw new SupportException(errorMessage, e);
        }

        log.trace("sendRequest end");
        return html;
    }
}
