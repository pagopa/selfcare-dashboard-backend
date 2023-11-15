package it.pagopa.selfcare.dashboard.core;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

    private final String supportApiKey;
    private final String zendeskOrganization;
    private final String returnTo;
    private static final String SUBDOMAIN  = "pagopa";

    public SupportServiceImpl(@Value("${support.api.key}") String supportApiKey,
                              @Value("${support.api.zendesk.redirectUri}") String returnTo,
                              @Value("${support.api.zendesk.organization}") String zendeskOrganization) {
        this.supportApiKey = supportApiKey;
        this.returnTo = returnTo;
        this.zendeskOrganization = zendeskOrganization;
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
                    .signWith(SignatureAlgorithm.HS256,supportApiKey.getBytes())
                    .compact();
        } catch(Exception e) {
            log.error("Impossible to sign zendesk jwt. Error: {}", e.getMessage(), e);
            throw new SupportException(e.getMessage());
        }
        String  redirectUrl = "https://" + SUBDOMAIN + ".zendesk.com/access/jwt?jwt=" + jwtString;
        log.debug("sendRequest result = {}", redirectUrl);
        log.trace("sendRequest end");

        String returnUrl = StringUtils.hasText(supportRequest.getProductId()) ?
                URLEncoder.encode(returnTo.concat("?product=" + supportRequest.getProductId()), StandardCharsets.UTF_8) :
                URLEncoder.encode(returnTo, StandardCharsets.UTF_8);

        return redirectUrl.concat("&return_to=" + returnUrl);
    }
}
