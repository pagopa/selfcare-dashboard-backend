package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class InstitutionInfo {

    private String institutionId;
    private String description;
    private String digitalAddress;
    private String status;
    private String category;
    private List<String> activeProducts = Collections.emptyList();

    public void setActiveProducts(List<String> activeProducts) {
        this.activeProducts = activeProducts == null
                ? Collections.emptyList()
                : activeProducts;
    }
}
