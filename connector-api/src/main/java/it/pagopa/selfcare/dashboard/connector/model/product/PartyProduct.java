package it.pagopa.selfcare.dashboard.connector.model.product;

import lombok.Data;

@Data
public class PartyProduct {
    private ProductOnBoardingStatus onBoardingStatus;
    private String id;
}