package it.pagopa.selfcare.dashboard.connector.rest.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Product {

    private String id;
    private ProductState state;

}
