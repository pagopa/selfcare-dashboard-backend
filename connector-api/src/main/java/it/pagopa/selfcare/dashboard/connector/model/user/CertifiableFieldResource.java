package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertifiableFieldResource<T> {

    private Certification certification;
    private T value;

}
