package it.pagopa.selfcare.dashboard.core;

public interface RelationshipService {

    void suspend(String relationshipId);

    void activate(String relationshipId);

    void delete(String relationshipId);

}
