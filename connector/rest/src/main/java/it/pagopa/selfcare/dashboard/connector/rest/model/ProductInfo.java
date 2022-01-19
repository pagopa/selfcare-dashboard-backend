package it.pagopa.selfcare.dashboard.connector.rest.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
public class ProductInfo {

    private String id;
    private String role;
    private OffsetDateTime createdAt;

}
