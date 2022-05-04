package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Data;

import java.util.Objects;

@Data
public class InstitutionInfo {

    private String institutionId;
    private String description;
    private String taxCode;
    private String digitalAddress;
    private String status;
    private String category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionInfo that = (InstitutionInfo) o;
        return institutionId.equals(that.institutionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(institutionId);
    }
}
