package it.pagopa.selfcare.dashboard.model.product;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BrokerResource {

    @ApiModelProperty(value = "${swagger.dashboard.brokers.model.code}")
    private String code;

    @ApiModelProperty(value = "${swagger.dashboard.brokers.model.description}")
    private String description;

    @ApiModelProperty(value = "${swagger.dashboard.brokers.model.enabled}")
    private Boolean enabled;

}
