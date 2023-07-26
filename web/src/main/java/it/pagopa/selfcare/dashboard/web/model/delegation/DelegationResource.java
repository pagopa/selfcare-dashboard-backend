package it.pagopa.selfcare.dashboard.web.model.delegation;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationType;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelegationResource {

        private String id;
        private String from;
        private String institutionFromName;
        private String institutionFromRootName;
        private DelegationType type;
        private String to;
        private String productId;

}
