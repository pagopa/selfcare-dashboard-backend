package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";
    static final String REQUIRED_GEOGRAPHIC_TAXONOMIES = "An object of geographic taxonomy list is required";
    static final String REQUIRED_UPDATE_RESOURCE_MESSAGE = "An Institution update resource is required";
    private final MsCoreConnector msCoreConnector;
    private final ProductsConnector productsConnector;

    @Autowired
    public InstitutionServiceImpl(ProductsConnector productsConnector,
                                  MsCoreConnector msCoreConnector) {
        this.productsConnector = productsConnector;
        this.msCoreConnector = msCoreConnector;
    }


    @Override
    public Institution getInstitutionById(String institutionId) {
        log.trace("getInstitution start");
        Institution result = msCoreConnector.getInstitution(institutionId);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitution result = {}", result);
        log.trace("getInstitution end");
        return result;
    }

    @Override
    public void updateInstitutionGeographicTaxonomy(String institutionId, GeographicTaxonomyList geographicTaxonomies) {
        log.trace("updateInstitutionGeographicTaxonomy start");
        log.debug("updateInstitutionGeographicTaxonomy institutiondId = {}, geographic taxonomies = {}", institutionId, geographicTaxonomies);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(geographicTaxonomies, REQUIRED_GEOGRAPHIC_TAXONOMIES);
        msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        log.trace("updateInstitutionGeographicTaxonomy end");
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String institutionId) {
        log.trace("getGeographicTaxonomyList start");
        log.debug("getGeographicTaxonomyList externalInstitutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        List<GeographicTaxonomy> result = msCoreConnector.getGeographicTaxonomyList(institutionId);
        log.debug("getGeographicTaxonomyList result = {}", result);
        log.trace("getGeographicTaxonomyList end");
        return result;
    }

    @Override
    public List<ProductTree> getProductsTree(){
        log.trace("getProductsTree start");
        List<ProductTree> productTrees = productsConnector.getProductsTree();
        log.debug("getInstitutionProducts result = {}", productTrees);
        log.trace("getInstitutionProducts end");
        return productTrees;
    }

    @Override
    public Institution updateInstitutionDescription(String institutionId, UpdateInstitutionResource updateInstitutionResource) {
        log.trace("updateInstitutionDescription start");
        log.debug("updateInstitutionDescription institutionId = {}, updateInstitutionResource = {}", institutionId, updateInstitutionResource);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Assert.notNull(updateInstitutionResource, REQUIRED_UPDATE_RESOURCE_MESSAGE);
        Institution institution = msCoreConnector.updateInstitutionDescription(institutionId, updateInstitutionResource);
        log.debug("updateInstitutionDescription result = {}", institution);
        log.trace("updateInstitutionDescription end");
        return institution;
    }

    @Override
    public Institution findInstitutionById(String institutionId) {
        log.trace("findInstitutionById start");
        log.debug("findInstitutionById institutionId = {}", institutionId);
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_MESSAGE);
        Institution institution = msCoreConnector.getInstitution(institutionId);
        if (institution != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(SelfCareGrantedAuthority.class::cast)
                    .filter(grantedAuthority -> institutionId.equals(grantedAuthority.getInstitutionId()))
                    .findAny();

            if (selcAuthority.isPresent()) {
                Map<String, ProductGrantedAuthority> userAuthProducts = ((SelfCareGrantedAuthority) selcAuthority.get()).getRoleOnProducts();

                if (LIMITED.name().equals(selcAuthority.get().getAuthority())) {
                    institution.setOnboarding(institution.getOnboarding().stream()
                            .filter(product -> userAuthProducts.containsKey(product.getProductId()))
                            .peek(product -> product.setAuthorized(true))
                            .peek(product -> product.setUserRole(LIMITED.name()))
                            .toList());
                } else {
                    institution.getOnboarding().forEach(product -> {
                        product.setAuthorized(userAuthProducts.containsKey(product.getProductId()));
                        Optional.ofNullable(userAuthProducts.get(product.getProductId()))
                                .ifPresentOrElse(authority -> product.setUserRole(authority.getAuthority()), () -> product.setUserRole(null));
                    });
                }
            }
        }
        log.debug("findInstitutionById result = {}", institution);
        log.trace("findInstitutionById end");
        return institution;
    }

}
