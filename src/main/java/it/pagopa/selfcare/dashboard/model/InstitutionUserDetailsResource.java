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

package it.pagopa.selfcare.dashboard.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstitutionUserDetailsResource extends InstitutionUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}")
    private String fiscalCode;

}
