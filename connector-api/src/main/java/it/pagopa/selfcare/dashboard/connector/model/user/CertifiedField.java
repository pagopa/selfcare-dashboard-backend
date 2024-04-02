package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertifiedField<T> {

    private Certification certification;
    private T value;

}
