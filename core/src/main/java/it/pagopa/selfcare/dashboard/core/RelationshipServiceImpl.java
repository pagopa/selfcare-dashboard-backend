package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
class RelationshipServiceImpl implements RelationshipService {

    private final PartyConnector partyConnector;


    @Autowired
    RelationshipServiceImpl(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }


    @Override
    public void suspend(String relationshipId) {
        Assert.hasText(relationshipId, "A Relationship id is required");

        partyConnector.suspend(relationshipId);
    }


    @Override
    public void activate(String relationshipId) {
        Assert.hasText(relationshipId, "A Relationship id is required");

        partyConnector.activate(relationshipId);
    }

    @Override
    public void delete(String relationshipId) {
        Assert.hasText(relationshipId, "A Relationship id is required");

        partyConnector.delete(relationshipId);
    }


}
