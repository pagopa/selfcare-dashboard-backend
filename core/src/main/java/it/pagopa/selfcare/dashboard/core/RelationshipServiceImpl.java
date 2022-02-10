package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
class RelationshipServiceImpl implements RelationshipService {

    private final PartyConnector partyConnector;
    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";

    @Autowired
    RelationshipServiceImpl(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }


    @Override
    public void suspend(String relationshipId) {
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        log.trace("suspend start");
        log.debug("suspend relationshipId = {}", relationshipId);
        partyConnector.suspend(relationshipId);
        log.trace("suspend end");

    }


    @Override
    public void activate(String relationshipId) {
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        log.trace("activate start");
        log.debug("activate relationshipId = {}", relationshipId);
        partyConnector.activate(relationshipId);
        log.trace("activate end");

    }

    @Override
    public void delete(String relationshipId) {
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        log.trace("delete start");
        log.debug("relationshipId = {}", relationshipId);
        partyConnector.delete(relationshipId);
        log.trace("delete end");

    }


}
