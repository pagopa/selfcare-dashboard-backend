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
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class InstitutionUserResource {

    @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String name;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String surname;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.email}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String email;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.role}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private SelfCareAuthority role;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.status}", required = true)
    @JsonProperty(required = true)
    @NotBlank
    private String status;

    @ApiModelProperty(value = "${swagger.dashboard.user.model.products}", required = true)
    @JsonProperty(required = true)
    @NotNull
    private List<ProductInfo> products;


    @Getter
    @Setter
    public static class ProductInfo {
        @ApiModelProperty(value = "${swagger.dashboard.products.model.id}", required = true)
        @JsonProperty(required = true)
        @NotBlank
        private String id;

        @ApiModelProperty(value = "${swagger.dashboard.products.model.title}", required = true)
        @JsonProperty(required = true)
        @NotBlank
        private String title;
    }

}
