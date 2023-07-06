package it.pagopa.selfcare.dashboard.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GeographicTaxonomyResource {
    @ApiModelProperty(value = "${swagger.dashboard.geographicTaxonomy.model.code}")
    private String code;

    @ApiModelProperty(value = "${swagger.dashboard.geographicTaxonomy.model.desc}")
    private String desc;
}
