package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelegationServiceImplTest {
    @Mock
    private MsCoreConnector delegationConnector;

    @InjectMocks
    private DelegationServiceImpl delegationServiceImpl;

    /**
     * Method under test: {@link DelegationServiceImpl#createDelegation(Delegation)}
     */
    @Test
    void testCreateDelegation() {
        DelegationId delegationId = new DelegationId();
        delegationId.setId("id");
        when(delegationConnector.createDelegation(any())).thenReturn(delegationId);
        Delegation delegation = new Delegation();
        delegation.setId("id");
        DelegationId response = delegationServiceImpl.createDelegation(delegation);
        verify(delegationConnector).createDelegation(any());
        assertNotNull(delegation);
        assertNotNull(delegation.getId());
        assertEquals(delegation.getId(), response.getId());
    }

}