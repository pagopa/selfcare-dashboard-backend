/*
 * Party Process Micro Service
 * This service is the party process
 *
 * OpenAPI spec version: {{version}}
 * Contact: support@example.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package it.pagopa.selfcare.dashboard.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InstitutionUserDetailsResource extends InstitutionUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String fiscalCode;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.certified}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private boolean certification;

}
