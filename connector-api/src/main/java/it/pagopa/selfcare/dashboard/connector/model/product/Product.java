package it.pagopa.selfcare.dashboard.connector.model.product;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
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
}
