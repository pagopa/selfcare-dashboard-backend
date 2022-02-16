package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import java.util.List;

@Data
public class ProductInfo {

    private String id;
    private String title;
    private List<RoleInfo> roleInfos;

}
