package it.pagopa.selfcare.dashboard.connector.model.institution;

import lombok.Data;

import java.util.Objects;

@Data
public class InstitutionInfo {

    private String id;
    private String origin;
    private String originId;
    private InstitutionType institutionType;
    private String externalId;
    private String description;
    private String address;
    private String zipCode;
    private String taxCode;
    private String digitalAddress;
    private RelationshipState status;
    private String category;
    private Billing billing;
    private PaymentServiceProvider paymentServiceProvider;
    private DataProtectionOfficer dataProtectionOfficer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionInfo that = (InstitutionInfo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
