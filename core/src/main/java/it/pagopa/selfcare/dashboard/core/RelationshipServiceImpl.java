package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
class RelationshipServiceImpl implements RelationshipService {

    private final MsCoreConnector coreConnector;
    private final UserGroupService userGroupService;
    private static final String REQUIRED_RELATIONSHIP_MESSAGE = "A Relationship id is required";
    
    @Override
    public void suspend(String relationshipId) {
        log.trace("suspend start");
        log.debug("suspend relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        coreConnector.suspend(relationshipId);
        log.trace("suspend end");

    }


    @Override
    public void activate(String relationshipId) {
        log.trace("activate start");
        log.debug("activate relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        coreConnector.activate(relationshipId);
        log.trace("activate end");

    }

    @Override
    public void delete(String relationshipId) {
        log.trace("delete start");
        log.debug("relationshipId = {}", relationshipId);
        Assert.hasText(relationshipId, REQUIRED_RELATIONSHIP_MESSAGE);
        coreConnector.delete(relationshipId);
        userGroupService.deleteMembersByRelationshipId(relationshipId);
        log.trace("delete end");
    }


}
