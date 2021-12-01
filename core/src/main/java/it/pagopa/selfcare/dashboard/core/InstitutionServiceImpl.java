package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    private final PartyConnector partyConnector;


    @Autowired
    public InstitutionServiceImpl(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }


    @Override
    public InstitutionInfo getInstitution(String institutionId) {
        return partyConnector.getInstitutionInfo(institutionId);
    }

}
