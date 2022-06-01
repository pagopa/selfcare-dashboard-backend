package it.pagopa.selfcare.dashboard.connector.rest.model.product;

import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import lombok.Data;

@Data
public class Product {

    private String id;
    private ProductState state;

}
