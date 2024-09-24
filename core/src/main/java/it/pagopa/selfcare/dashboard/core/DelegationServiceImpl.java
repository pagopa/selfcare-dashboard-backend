package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.pagopa.selfcare.onboarding.common.ProductId.PROD_PAGOPA;

@Slf4j
@Service
class DelegationServiceImpl implements DelegationService {

    private static final String INSTITUTION_TAX_CODE_NOT_FOUND = "Cannot find Institution using taxCode %s";
    private final MsCoreConnector msCoreConnector;


    @Autowired
    public DelegationServiceImpl(MsCoreConnector msCoreConnector) {
        this.msCoreConnector = msCoreConnector;
    }

    @Override
    public DelegationId createDelegation(DelegationRequest delegation) {
        log.trace("createDelegation start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "createDelegation request = {}", delegation);

        /*
            In case of prod-pagopa product, in the attribute "to" of the delegation object a taxCode is inserted.
            So we have to retrieve the institutionId from the taxCode and set it in the "to" attribute.
         */
        if(PROD_PAGOPA.getValue().equals(delegation.getProductId())) {
            setToInstitutionId(delegation);
        }

        DelegationId result = msCoreConnector.createDelegation(delegation);
        log.debug("createDelegation result = {}", result);
        log.trace("createDelegation end");
        return result;
    }

    private void setToInstitutionId(DelegationRequest delegation) {
        List<Institution> institutions = msCoreConnector.getInstitutionsFromTaxCode(delegation.getTo(), null, null, null);
        Institution partner = institutions.stream()
                .filter(institution -> institution.getInstitutionType() == InstitutionType.PT)
                .findFirst()
                .orElse(institutions.stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format(INSTITUTION_TAX_CODE_NOT_FOUND, delegation.getTo()))
                        ));
        delegation.setTo(partner.getId());
    }

    @Override
    public List<Delegation> getDelegations(GetDelegationParameters delegationParameters) {
        log.trace("getDelegations start");
        List<Delegation> result = msCoreConnector.getDelegations(delegationParameters);
        log.debug("getDelegations result = {}", result);
        log.trace("getDelegations end");
        return result;
    }

    @Override
    public DelegationWithPagination getDelegationsV2(GetDelegationParameters delegationParameters) {
        log.trace("getDelegationsV2 start");
        DelegationWithPagination result = msCoreConnector.getDelegationsV2(delegationParameters);
        log.debug("getDelegationsV2 result = {}", result);
        log.trace("getDelegationsV2 end");
        return result;
    }


}