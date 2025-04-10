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

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.dashboard.model.institution.Billing;
import it.pagopa.selfcare.dashboard.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.model.product.ProductInfo;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RelationshipInfo {

    private String id;
    private String from;
    private String to;
    private PartyRole role;
    private ProductInfo product;
    private RelationshipState state;
    private String pricingPlan;
    private InstitutionUpdate institutionUpdate;
    private Billing billing;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
