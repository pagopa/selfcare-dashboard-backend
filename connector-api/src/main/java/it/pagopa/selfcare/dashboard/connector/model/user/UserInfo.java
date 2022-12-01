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

package it.pagopa.selfcare.dashboard.connector.model.user;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class UserInfo {

    private String id;
    private User user;
    private SelfCareAuthority role;
    private Map<String, ProductInfo> products;
    private String status;
    private String institutionId;


    @Data
    public static class UserInfoFilter {
        private Optional<SelfCareAuthority> role = Optional.empty();
        private Optional<String> productId = Optional.empty();
        private Optional<Set<String>> productRoles = Optional.empty();
        private Optional<String> userId = Optional.empty();
        private Optional<EnumSet<RelationshipState>> allowedStates = Optional.empty();

        public void setRole(Optional<SelfCareAuthority> role) {
            this.role = role == null ? Optional.empty() : role;
        }

        public void setProductId(Optional<String> productId) {
            this.productId = productId == null ? Optional.empty() : productId;
        }

        public void setProductRoles(Optional<Set<String>> productRoles) {
            this.productRoles = productRoles == null ? Optional.empty() : productRoles;
        }

        public void setUserId(Optional<String> userId) {
            this.userId = userId == null ? Optional.empty() : userId;
        }

        public void setAllowedState(Optional<EnumSet<RelationshipState>> allowedStates) {
            this.allowedStates = allowedStates == null ? Optional.empty() : allowedStates;
        }
    }

}
