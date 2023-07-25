package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.PagoPABackOfficeConnector;
import it.pagopa.selfcare.dashboard.connector.model.backoffice.BrokerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionType.PSP;

@Slf4j
@Service
public class BrokerServiceImpl implements BrokerService {

    private final PagoPABackOfficeConnector pagoPABackOfficeConnector;
    private final MsCoreConnector msCoreConnector;

    public BrokerServiceImpl(PagoPABackOfficeConnector backofficeConnector,
                             MsCoreConnector msCoreConnector) {
        this.pagoPABackOfficeConnector = backofficeConnector;
        this.msCoreConnector = msCoreConnector;
    }

    private static final int DEFAULT_LIMIT = 1000;
    private static final int START_PAGE = 1;

    @Override
    public List<BrokerInfo> findAllByInstitutionType(String institutionType) {
        log.trace("findAllByInstitutionType start");
        List<BrokerInfo> brokers;
        if(PSP.name().equals(institutionType)) {
            brokers = pagoPABackOfficeConnector.getBrokersPSP(START_PAGE, DEFAULT_LIMIT);
        }else {
            brokers = pagoPABackOfficeConnector.getBrokersEC(START_PAGE, DEFAULT_LIMIT);
        }
        log.debug("findAllByInstitutionType result = {}", brokers);
        log.trace("findAllByInstitutionType end");
        return brokers;
    }

    @Override
    public List<BrokerInfo> findInstitutionsByProductAndType(String productId, String institutionType) {
        log.trace("findInstitutionsByProductAndType start");
        log.debug("findInstitutionsByProductAndType productId = {}, type = {}", productId, institutionType);
        List<BrokerInfo> brokers = msCoreConnector.findInstitutionsByProductAndType(productId, institutionType);
        log.debug("findInstitutionsByProductAndType result = {}", brokers);
        log.trace("findInstitutionsByProductAndType end");
        return brokers;
    }
}
