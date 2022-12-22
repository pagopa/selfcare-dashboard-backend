package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GeographicTaxonomyListDto {
    @ApiModelProperty(value = "${swagger.dashboard.geographicTaxonomyList.model.geographicTaxonomyDtoList}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private List<GeographicTaxonomyDto> geographicTaxonomyDtoList;
}
