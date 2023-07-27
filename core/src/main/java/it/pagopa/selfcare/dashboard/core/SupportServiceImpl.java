package it.pagopa.selfcare.dashboard.core;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

    private final String supportApiKey;
    private static final String SUBDOMAIN  = "pagopa";
    private static final String ZENDESK_ORGANIZATION = "_users_hc_selfcare";
    private static final String RETURN_TO = "https://selfcare.assistenza.pagopa.it/hc/it/requests/new";

    public SupportServiceImpl(@Value("${support.api.key}") String supportApiKey) {
        this.supportApiKey = supportApiKey;
    }

    @Override
    public String sendRequest(SupportRequest supportRequest) {

        log.trace("sendRequest start");
        log.debug("sendRequest request = {}", supportRequest);
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaims = buildJwtClaims(supportRequest);
        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaims.toJSONObject()));

        try {
            JWSSigner signer = new MACSigner(supportApiKey.getBytes());
            jwsObject.sign(signer);
        } catch(Exception e) {
            log.error("Error signing JWT: {}", e.getMessage(), e);
            throw new SupportException(e.getMessage());
        }

        String jwtString = jwsObject.serialize();
        String  redirectUrl = "https://" + SUBDOMAIN + ".zendesk.com/access/jwt?jwt=" + jwtString;
        log.debug("sendRequest result = {}", redirectUrl);
        log.trace("sendRequest end");
        return redirectUrl.concat("&return_to=" + URLEncoder.encode(RETURN_TO, StandardCharsets.UTF_8));
    }

    private JWTClaimsSet buildJwtClaims(SupportRequest supportRequest) {
        return new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .issueTime(new Date())
                .claim("name", supportRequest.getName())
                .claim("email", supportRequest.getEmail())
                .claim("organization", ZENDESK_ORGANIZATION)
                .claim("user_fields", supportRequest.getUserFields())
                .build();
    }

}
