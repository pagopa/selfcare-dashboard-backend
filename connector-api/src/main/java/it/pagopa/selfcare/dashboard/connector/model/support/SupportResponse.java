package it.pagopa.selfcare.dashboard.connector.model.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class SupportResponse {
    private String redirectUrl;
}
