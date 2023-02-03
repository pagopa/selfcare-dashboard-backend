package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingPnPGInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingPnPGData;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;

@Slf4j
@Service
class MsCoreConnectorImpl implements MsCoreConnector {

    private static final BinaryOperator<InstitutionInfo> MERGE_FUNCTION = (inst1, inst2) -> {
        if (ACTIVE.equals(inst1.getStatus())) {
            return inst1;
        } else if (PENDING.equals(inst1.getStatus())) {
            return inst1;
        } else {
            return inst2;
        }
    };

    private static final Function<OnboardingPnPGData, InstitutionInfo> ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION = onboardingData -> {
        InstitutionInfo institutionInfo = new InstitutionInfo();
        institutionInfo.setOriginId(onboardingData.getOriginId());
        institutionInfo.setId(onboardingData.getId());
        institutionInfo.setOrigin(onboardingData.getOrigin());
        institutionInfo.setInstitutionType(onboardingData.getInstitutionType());
        institutionInfo.setExternalId(onboardingData.getExternalId());
        institutionInfo.setDescription(onboardingData.getDescription());
        institutionInfo.setTaxCode(onboardingData.getTaxCode());
        institutionInfo.setDigitalAddress(onboardingData.getDigitalAddress());
        institutionInfo.setStatus(onboardingData.getState());
        institutionInfo.setAddress(onboardingData.getAddress());
        institutionInfo.setZipCode(onboardingData.getZipCode());
        institutionInfo.setBilling(onboardingData.getBilling());
        if (onboardingData.getGeographicTaxonomies() == null) {
            throw new ValidationException(String.format("The institution %s does not have geographic taxonomies.", institutionInfo.getId()));
        } else {
            institutionInfo.setGeographicTaxonomies(onboardingData.getGeographicTaxonomies());
        }
        if (onboardingData.getAttributes() != null && !onboardingData.getAttributes().isEmpty()) {
            institutionInfo.setCategory(onboardingData.getAttributes().get(0).getDescription());
        }
        return institutionInfo;
    };


    private final MsCoreRestClient msCoreRestClient;


    @Autowired
    public MsCoreConnectorImpl(MsCoreRestClient msCoreRestClient) {
        this.msCoreRestClient = msCoreRestClient;
    }


    @Override
    public Collection<InstitutionInfo> getOnBoardedInstitutions() {
        log.trace("getOnBoardedInstitutions start");
        OnBoardingPnPGInfo onBoardingInfo = msCoreRestClient.getOnBoardingInfo(null, null, EnumSet.of(ACTIVE, PENDING, TOBEVALIDATED));
        Collection<InstitutionInfo> result = parseOnBoardingInfo(onBoardingInfo);
        log.debug("getOnBoardedInstitutions result = {}", result);
        log.trace("getOnBoardedInstitutions end");
        return result;
    }

    private Collection<InstitutionInfo> parseOnBoardingInfo(OnBoardingPnPGInfo onBoardingInfo) {
        log.trace("parseOnBoardingInfo start");
        log.debug("parseOnBoardingInfo onBoardingInfo = {}", onBoardingInfo);
        Collection<InstitutionInfo> institutions = Collections.emptyList();
        if (onBoardingInfo != null && onBoardingInfo.getInstitutions() != null) {
            institutions = onBoardingInfo.getInstitutions().stream()
                    .map(ONBOARDING_DATA_TO_INSTITUTION_INFO_FUNCTION)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(InstitutionInfo::getId, Function.identity(), MERGE_FUNCTION),
                            Map::values
                    ));
        }
        log.debug("parseOnBoardingInfo result = {}", institutions);
        log.trace("parseOnBoardingInfo end");
        return institutions;
    }


    private static final Function<Product, PartyProduct> PRODUCT_INFO_TO_PRODUCT_FUNCTION = productInfo -> {
        PartyProduct product = new PartyProduct();
        product.setId(productInfo.getId());
        product.setOnBoardingStatus(ProductOnBoardingStatus.valueOf(productInfo.getState().toString()));
        return product;
    };

    @Override
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);
        List<PartyProduct> products = Collections.emptyList();
        Products institutionProducts = msCoreRestClient.getInstitutionProducts(institutionId, EnumSet.of(ProductState.ACTIVE, ProductState.PENDING));
        if (institutionProducts != null && institutionProducts.getProducts() != null) {
            products = institutionProducts.getProducts().stream()
                    .map(PRODUCT_INFO_TO_PRODUCT_FUNCTION)
                    .collect(Collectors.toList());
        }
        log.debug("getInstitutionProducts result = {}", products);
        log.trace("getInstitutionProducts end");
        return products;
    }

}
