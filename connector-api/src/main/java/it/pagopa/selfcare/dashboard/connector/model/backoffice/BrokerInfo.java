package it.pagopa.selfcare.dashboard.connector.model.backoffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrokerInfo {

    private String brokerCode;
    private String brokerPspCode;
    private String description;
    private Boolean enabled;
    private Boolean extendedFaultBean;

}
