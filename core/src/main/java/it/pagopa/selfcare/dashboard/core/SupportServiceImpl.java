package it.pagopa.selfcare.dashboard.core;

import freemarker.template.Template;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.UserField;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.*;

import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;

@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

    private final String supportApiKey;
    private final String zendeskOrganization;
    private final String returnTo;
    private final String actionUrl;
    private final UserRegistryConnector userRegistryConnector;
    @Qualifier("zendeskFreeMarker")
    private final FreeMarkerConfigurer freeMarkerConfigurer;
    private static final EnumSet<User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, fiscalCode);

    public SupportServiceImpl(@Value("${support.api.key}") String supportApiKey,
                              @Value("${support.api.zendesk.redirectUri}") String returnTo,
                              @Value("${support.api.zendesk.organization}") String zendeskOrganization,
                              @Value("${support.api.zendesk.actionUri}") String redirectUrl,
                              FreeMarkerConfigurer freeMarkerConfigurer,
                              UserRegistryConnector userRegistryConnector) {
        this.supportApiKey = supportApiKey;
        this.returnTo = returnTo;
        this.zendeskOrganization = zendeskOrganization;
        this.actionUrl = redirectUrl;
        this.freeMarkerConfigurer = freeMarkerConfigurer;
        this.userRegistryConnector = userRegistryConnector;
    }

    @Override
    public String sendRequest(SupportRequest supportRequest) {

        log.trace("sendRequest start");
        log.debug("sendRequest request = {}", supportRequest);

        if(StringUtils.hasText(supportRequest.getUserId())) {
            try {
                User user = userRegistryConnector.getUserByInternalId(supportRequest.getUserId(), USER_FIELD_LIST);
                supportRequest.setName(user.getName().getValue().concat(" " + user.getFamilyName().getValue()));
                supportRequest.setUserFields(UserField.builder().aux_data(user.getFiscalCode()).build());
            } catch (Exception e) {
                throw new ResourceNotFoundException("User with id " + supportRequest.getUserId() + " not found");
            }
        }

        //Retrieve parameters for submitting form
        String redirectUrl = getRedirectUrl(supportRequest);
        String jwtString = getJWTString(supportRequest);

        String html;
        try {
            Map<String, String> map = Map.of(
                    "jwt", jwtString,
                    "returnTo", redirectUrl,
                    "action", actionUrl
            );
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

    private String getJWTString(SupportRequest supportRequest) {
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
        return jwtString;
    }

    private String getRedirectUrl(SupportRequest supportRequest) {
        StringBuilder urlBuilder = new StringBuilder(returnTo);
        if(Objects.nonNull(supportRequest.getProductId())) {
            urlBuilder.append("?product=").append(supportRequest.getProductId());
        }
        if(Objects.nonNull(supportRequest.getInstitutionId())) {
            urlBuilder.append(urlBuilder.indexOf("?") != -1 ?
                    "&institution=" + supportRequest.getInstitutionId()
                    : "?product=" + supportRequest.getInstitutionId());
        }
        return urlBuilder.toString();
    }
}
