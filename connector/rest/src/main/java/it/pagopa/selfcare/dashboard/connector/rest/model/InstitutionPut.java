package it.pagopa.selfcare.dashboard.connector.rest.model;

import lombok.Data;

import java.util.List;

@Data
public class InstitutionPut {
    private List<String> geographicTaxonomyCodes;
}
