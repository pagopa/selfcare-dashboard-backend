package it.pagopa.selfcare.dashboard.connector.rest;

import io.github.resilience4j.retry.annotation.Retry;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.auth.ProductRole;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationRequest;
import it.pagopa.selfcare.dashboard.connector.model.delegation.GetDelegationParameters;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreDelegationApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreInstitutionApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.client.CoreOnboardingApiRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.DelegationRestClientMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.InstitutionMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
class CoreConnectorImpl implements MsCoreConnector {


    private final CoreInstitutionApiRestClient coreInstitutionApiRestClient;
    private final CoreDelegationApiRestClient coreDelegationApiRestClient;
    private final CoreOnboardingApiRestClient coreOnboardingApiRestClient;
    private final BrokerMapper brokerMapper;
    private final InstitutionMapper institutionMapper;
    private final DelegationRestClientMapper delegationMapper;

    static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    static final String REQUIRED_PRODUCT_ID_MESSAGE = "A Product id is required";
    static final String REQUIRED_INSTITUTION_TYPE_MESSAGE = "An Institution type is required";
    static final String REQUIRED_UPDATE_RESOURCE_MESSAGE = "An Institution description is required";
    static final String REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE = "An object of geographic taxonomy list is required";

    @Override
    @Retry(name = "retryTimeout")
    public Institution getInstitution(String institutionId) {
        log.trace("getInstitution start");
        log.debug("getInstitution institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        InstitutionResponse institution = coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId).getBody();
        log.debug("getInstitution result = {}", institution);
        log.trace("getInstitution end");
        return institutionMapper.toInstitution(institution);
    }

    @Override
    public Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updateInstitutionResource) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, updateInstitutionResource = {}", institutionId, updateInstitutionResource);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.notNull(updateInstitutionResource, REQUIRED_UPDATE_RESOURCE_MESSAGE);
        InstitutionResponse institution = coreInstitutionApiRestClient._updateInstitutionUsingPUT(institutionId, institutionMapper.toInstitutionPut(updateInstitutionResource)).getBody();
        log.debug("updateInstitutionDescription result = {}", institution);
        log.trace("updateInstitutionDescription end");
        return institutionMapper.toInstitution(institution);
    }

    @Override
    public DelegationId createDelegation(DelegationRequest delegation) {
        log.trace("createDelegation start");
        log.debug("createDelegation request = {}", delegation.toString());
        DelegationId delegationId = new DelegationId();
        DelegationResponse result = coreDelegationApiRestClient._createDelegationUsingPOST(delegationMapper.toDelegationRequest(delegation)).getBody();
        log.debug("updateInstitutionDescription result = {}", result);
        log.trace("updateInstitutionDescription end");
        if (result != null) {
            delegationId.setId(result.getId());
        }
        return delegationId;
    }

    @Override
    @Retry(name = "retryTimeout")
    public List<BrokerInfo> findInstitutionsByProductAndType(String productId, String type) {
        log.trace("findInstitutionsByProductAndType start");
        log.debug("findInstitutionsByProductAndType productId = {}, type = {}", productId, type);
        Assert.hasText(productId, REQUIRED_PRODUCT_ID_MESSAGE);
        Assert.hasText(type, REQUIRED_INSTITUTION_TYPE_MESSAGE);
        List<BrokerResponse> brokerResponses = coreInstitutionApiRestClient._getInstitutionBrokersUsingGET(productId, type).getBody();
        List<BrokerInfo> brokers = brokerMapper.fromInstitutions(brokerResponses);
        log.debug("findInstitutionsByProductAndType result = {}", brokers);
        log.trace("findInstitutionsByProductAndType end");
        return brokers;
    }

    @Override
    @Retry(name = "retryTimeout")
    public List<Delegation> getDelegations(GetDelegationParameters delegationParameters) {
        log.trace("getDelegations start");
        log.debug("getDelegations productId = {}, type = {}", delegationParameters.getFrom(), delegationParameters.getProductId());
        List<DelegationResponse> delegationsResponse = coreDelegationApiRestClient._getDelegationsUsingGET(
                delegationParameters.getFrom(),
                delegationParameters.getTo(),
                delegationParameters.getProductId(),
                delegationParameters.getSearch(),
                delegationParameters.getTaxCode(),
                delegationParameters.getMode(),
                delegationParameters.getOrder(),
                delegationParameters.getPage(),
                delegationParameters.getSize())
                .getBody();

        if (Objects.isNull(delegationsResponse))
            return List.of();

        List<Delegation> delegations = delegationsResponse.stream()
                .map(delegationMapper::toDelegations)
                .toList();
        log.debug("getDelegations result = {}", delegations);
        log.trace("getDelegations end");
        return delegations;
    }

    @Override
    public void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies) {
        log.trace("updateInstitutionGeographicTaxonomy start");
        log.debug("updateInstitutionGeographicTaxonomy institutionId = {}, geograpihc taxonomies = {}", institutionId, geographicTaxonomies);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.notNull(geographicTaxonomies, REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE);
        InstitutionPut geographicTaxonomiesRequest = new InstitutionPut();
        geographicTaxonomiesRequest.setGeographicTaxonomyCodes(geographicTaxonomies.getGeographicTaxonomyList().stream().map(GeographicTaxonomy::getCode).toList());
        coreInstitutionApiRestClient._updateInstitutionUsingPUT(institutionId, geographicTaxonomiesRequest);
        log.trace("updateInstitutionGeographicTaxonomy end");
    }

    @Override
    @Retry(name = "retryTimeout")
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        InstitutionResponse institution = coreInstitutionApiRestClient._retrieveInstitutionByIdUsingGET(institutionId).getBody();
        List<GeographicTaxonomy> result = Collections.emptyList();
        if (institution != null && !CollectionUtils.isEmpty(institution.getGeographicTaxonomies())) {
            result = institutionMapper.toGeographicTaxonomy(institution.getGeographicTaxonomies());
        }
        if (CollectionUtils.isEmpty(result)) {
            throw new ValidationException(String.format("The institution %s does not have geographic taxonomies.", institutionId));
        }

        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    @Retry(name = "retryTimeout")
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);
        List<PartyProduct> products = Collections.emptyList();
        OnboardedProducts institutionProducts = coreInstitutionApiRestClient._retrieveInstitutionProductsUsingGET(institutionId, ProductState.ACTIVE.name() + "," + ProductState.PENDING.name()).getBody();
        if (institutionProducts != null && !CollectionUtils.isEmpty(institutionProducts.getProducts())) {
            products = institutionProducts.getProducts().stream()
                    .map(institutionMapper::toPartyProduct)
                    .toList();
        }
        log.debug("getInstitutionProducts result = {}", products);
        log.trace("getInstitutionProducts end");
        return products;
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected static class PartyProductRole implements ProductRole {
        protected String productRole;
        protected String productId;
        protected PartyRole partyRole;
    }


    @Getter
    @Setter(AccessLevel.PROTECTED)
    protected static class PartyAuthInfo implements AuthInfo {
        protected String institutionId;
        protected Collection<ProductRole> productRoles;
    }


}
