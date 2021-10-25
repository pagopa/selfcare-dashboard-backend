package it.pagopa.selfcare.dashboard.web.security;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;

@Data
public class JwtAuthenticationDetails {

    private final String institutionId;

    public JwtAuthenticationDetails(HttpServletRequest request) {
        institutionId = request.getHeader("x-institutionId");// TODO: define header name
    }
}
