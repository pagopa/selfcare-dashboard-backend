package it.pagopa.selfcare.dashboard.connector.model.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartyProduct {
    private ProductStatus status;
    private String productId;
}