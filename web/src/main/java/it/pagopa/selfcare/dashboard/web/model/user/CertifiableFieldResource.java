package it.pagopa.selfcare.dashboard.web.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertifiableFieldResource<T> {
    private boolean certified;
    private T value;
}
