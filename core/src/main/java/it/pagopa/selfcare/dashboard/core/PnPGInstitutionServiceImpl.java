package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.PartyRegistryProxyConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.PnPGInstitutionLegalAddressData;
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

    private final MsCoreConnector msCoreConnector;

    private final PartyRegistryProxyConnector partyRegistryProxyConnector;


    @Autowired
    public PnPGInstitutionServiceImpl(PartyRegistryProxyConnector partyRegistryProxyConnector,
                                      MsCoreConnector msCoreConnector) {
        this.partyRegistryProxyConnector = partyRegistryProxyConnector;
        this.msCoreConnector = msCoreConnector;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions() {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = msCoreConnector.getOnBoardedInstitutions();
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
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

    @Override
    public PnPGInstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress externalInstitutionId = {}", externalInstitutionId);
        PnPGInstitutionLegalAddressData result = partyRegistryProxyConnector.getInstitutionLegalAddress(externalInstitutionId);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

}
