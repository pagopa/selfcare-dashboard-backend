package it.pagopa.selfcare.dashboard.web.security;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_ADMIN("Amministratore"),
    ROLE_LEGAL("Rappresentante Legale dell'ente"),
    ROLE_ADMIN_REF("Referente Amministrativo"),
    ROLE_TECH_REF("Referente Tecnico"),
    ROLE_REVIEWER("Reviewer PagoPA"),
    ROLE_USER("Utente qualsiasi autenticato");

    private String name;

    Role(String name) {
        this.name = name;
    }

}
