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
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.model.product.ProductInfoResource;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class InstitutionUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}")
    private UUID id;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}")
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}")
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.email}")
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalPhone}")
    private String mobilePhone;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.role}")
    private SelfCareAuthority role;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.status}")
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.products}")
    private List<ProductInfoResource> products;

}
