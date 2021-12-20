package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RelationshipServiceImplTest {

    @Mock
    private PartyConnector partyConnectorMock;

    @InjectMocks
    private RelationshipServiceImpl relationshipService;


    @Test
    void suspend_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> relationshipService.suspend(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Relationship id is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void suspend() {
        // given
        String relationshipId = "relationshipId";
        // when
        relationshipService.suspend(relationshipId);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .suspend(relationshipId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void activate_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> relationshipService.activate(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Relationship id is required", e.getMessage());
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void activate() {
        // given
        String relationshipId = "relationshipId";
        // when
        relationshipService.activate(relationshipId);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .activate(relationshipId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

}