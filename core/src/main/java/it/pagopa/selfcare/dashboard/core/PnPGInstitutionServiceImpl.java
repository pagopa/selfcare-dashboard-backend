package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
class PnPGInstitutionServiceImpl implements PnPGInstitutionService {

    static final String REQUIRED_INSTITUTION_MESSAGE = "An Institution id is required";

    static final String REQUIRED_UPDATE_RESOURCE_MESSAGE = "An Institution update resource is required";

    private final MsCoreConnector msCoreConnector;

    @Autowired
    public PnPGInstitutionServiceImpl(MsCoreConnector msCoreConnector) {
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public List<PartyProduct> getInstitutionProducts(String institutionId) {
        log.trace("getInstitutionProducts start");
        log.debug("getInstitutionProducts institutionId = {}", institutionId);

        Map<String, PartyProduct> institutionsProductsMap = msCoreConnector.getInstitutionProducts(institutionId).stream()
                .collect(Collectors.toMap(PartyProduct::getId, Function.identity()));

        List<PartyProduct> listProducts = new ArrayList<>(institutionsProductsMap.values());

        log.debug("getInstitutionProducts result = {}", listProducts);
        log.trace("getInstitutionProducts end");
        return listProducts;
    }

}
