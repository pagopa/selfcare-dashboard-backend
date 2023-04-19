package it.pagopa.selfcare.dashboard.connector.rest.model.product;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PnPGProductInfo {

    private String id;
    private List<String> role;
    private OffsetDateTime createdAt;

}
