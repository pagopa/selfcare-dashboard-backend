package it.pagopa.selfcare.dashboard.connector.model.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductTree {
    private Product node;
    private List<Product> children;
}
