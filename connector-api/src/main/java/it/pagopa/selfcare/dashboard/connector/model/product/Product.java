package it.pagopa.selfcare.dashboard.connector.model.product;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Product {

    private String id;
    private String logo;
    private String title;
    private String description;
    private String urlPublic;
    private String urlBO;
    private OffsetDateTime activationDateTime;
}
