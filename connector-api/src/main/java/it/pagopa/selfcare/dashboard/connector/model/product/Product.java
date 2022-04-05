package it.pagopa.selfcare.dashboard.connector.model.product;

import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.EnumMap;

@Data
public class Product {

    private String id;
    private String logo;
    private String title;
    private String description;
    private String urlPublic;
    private String urlBO;
    private OffsetDateTime activatedAt;
    private boolean authorized;
    private String userRole;
    private ProductStatus status;
    private EnumMap<PartyRole, ProductRoleInfo> roleMappings;

}
