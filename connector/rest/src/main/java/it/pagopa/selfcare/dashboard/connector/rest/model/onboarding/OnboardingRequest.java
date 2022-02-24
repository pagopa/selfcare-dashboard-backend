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

package it.pagopa.selfcare.dashboard.connector.rest.model.onboarding;

import lombok.Data;

import java.util.List;

@Data
public class OnboardingRequest {

    private String institutionId;
    private List<User> users;
    private OnboardingContract contract;

}
