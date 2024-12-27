package it.pagopa.selfcare.dashboard.integration_test.model;

import it.pagopa.selfcare.dashboard.model.IdentityTokenResource;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationIdResource;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationResource;
import it.pagopa.selfcare.dashboard.model.delegation.DelegationWithPagination;
import it.pagopa.selfcare.dashboard.model.product.BrokerResource;
import it.pagopa.selfcare.dashboard.model.product.ProductRoleMappingsResource;
import it.pagopa.selfcare.dashboard.model.support.SupportResponse;
import it.pagopa.selfcare.dashboard.model.user_groups.UserGroupIdResource;
import it.pagopa.selfcare.dashboard.model.user_groups.UserGroupPlainResource;
import it.pagopa.selfcare.dashboard.model.user_groups.UserGroupResource;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@Data
public class Responses {
    protected IdentityTokenResource identityTokenResource;
    protected SupportResponse supportResponse;
    protected URI backOfficeUrl;
    protected List<BrokerResource> brokerResource;
    protected List<ProductRoleMappingsResource> productRoleMappingsResource;
    private DelegationWithPagination delegationWithPagination;
    private List<DelegationResource> delegationResource;
    private DelegationIdResource delegationIdResource;
    private UserGroupIdResource userGroupIdResource;
    private Page<UserGroupPlainResource> userGroupPlainResource;
    private UserGroupResource userGroupResource;
}
