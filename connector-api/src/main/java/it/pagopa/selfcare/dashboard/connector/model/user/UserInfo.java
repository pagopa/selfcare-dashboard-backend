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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public class UserInfo {

    private String id;
    private String name;
    private String surname;
    private String email;
    private String taxCode;
    private SelfCareAuthority role;
    private boolean certified;
    private Map<String, ProductInfo> products;
    private String status;


    @Data
    public static class UserInfoFilter {
        private Optional<SelfCareAuthority> role = Optional.empty();
        private Optional<String> productId = Optional.empty();
        private Optional<Set<String>> productRoles = Optional.empty();
        private Optional<String> userId = Optional.empty();

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
    }

}
