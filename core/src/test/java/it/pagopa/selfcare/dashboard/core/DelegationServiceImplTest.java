package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.delegation.Delegation;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationId;
import it.pagopa.selfcare.dashboard.connector.model.delegation.DelegationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    void getDelegations() {
        //given
        Delegation delegation = dummyDelegation();
        List<Delegation> delegationList = new ArrayList<>();
        delegationList.add(delegation);
        when(delegationConnector.getDelegations(any(),any(),any())).thenReturn(delegationList);

        //when
        delegationList = delegationServiceImpl.getDelegations(delegation.getFrom(), delegation.getTo(), delegation.getProductId());

        //then
        assertNotNull(delegationList);
        assertNotNull(delegationList.getClass());
        assertEquals(1, delegationList.size());
        verify(delegationConnector, times(1))
                .getDelegations(any(), any(),any());
        verifyNoMoreInteractions(delegationConnector);
    }

    private Delegation dummyDelegation() {
        Delegation delegation = new Delegation();
        delegation.setFrom("from");
        delegation.setTo("to");
        delegation.setId("setId");
        delegation.setProductId("setProductId");
        delegation.setType(DelegationType.PT);
        delegation.setInstitutionFromName("setInstitutionFromName");
        return delegation;
    }

}