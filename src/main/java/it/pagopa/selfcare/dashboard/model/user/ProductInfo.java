package it.pagopa.selfcare.dashboard.model.user;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProductInfo {

    private String id;
    private String title;
    private List<RoleInfo> roleInfos;
    private OffsetDateTime createdAt;

}
