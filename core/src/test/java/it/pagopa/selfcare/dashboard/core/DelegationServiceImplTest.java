package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
     * Method under test: {@link DelegationServiceImpl#createDelegation(DelegationRequest)}
     */
    @Test
    void testCreateDelegation() {
        DelegationId delegationId = new DelegationId();
        delegationId.setId("id");
        when(delegationConnector.createDelegation(any())).thenReturn(delegationId);
        DelegationRequest delegation = new DelegationRequest();
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
        when(delegationConnector.getDelegations(any())).thenReturn(delegationList);

        //when
        delegationList = delegationServiceImpl.getDelegations(dummyDelegationParametersTo());

        //then
        assertNotNull(delegationList);
        assertNotNull(delegationList.getClass());
        assertEquals(1, delegationList.size());
        verify(delegationConnector, times(1))
                .getDelegations(dummyDelegationParametersTo());
        verifyNoMoreInteractions(delegationConnector);
    }

    @Test
    void getDelegationsV2() {
        //given
        DelegationWithInfo delegation = dummyDelegationWithInfo();
        List<DelegationWithInfo> delegationList = new ArrayList<>();
        delegationList.add(delegation);
        PageInfo pageInfo = new PageInfo(1000, 0, 1, 1);
        DelegationWithPagination delegationWithPagination= new DelegationWithPagination(delegationList, pageInfo);

        when(delegationConnector.getDelegationsV2(any())).thenReturn(delegationWithPagination);

        //when
        DelegationWithPagination response = delegationServiceImpl.getDelegationsV2(dummyDelegationParametersTo());

        //then
        assertNotNull(response);
        assertEquals(1, response.getDelegations().size());
        assertEquals(delegation, response.getDelegations().get(0));
        verify(delegationConnector, times(1))
                .getDelegationsV2(dummyDelegationParametersTo());
        verifyNoMoreInteractions(delegationConnector);
    }

    private Delegation dummyDelegation() {
        Delegation delegation = new Delegation();
        delegation.setInstitutionId("from");
        delegation.setBrokerId("to");
        delegation.setId("setId");
        delegation.setProductId("setProductId");
        delegation.setType(DelegationType.PT);
        delegation.setInstitutionName("setInstitutionFromName");
        return delegation;
    }

    private DelegationWithInfo dummyDelegationWithInfo() {
        DelegationWithInfo delegation = new DelegationWithInfo();
        delegation.setId("setId");
        delegation.setInstitutionId("from");
        delegation.setInstitutionName("setInstitutionFromName");
        delegation.setBrokerId("to");
        delegation.setBrokerName("setInstitutionFromRootName");
        delegation.setBrokerTaxCode("brokerTaxCode");
        delegation.setBrokerType("brokerType");
        delegation.setProductId("setProductId");
        delegation.setInstitutionRootName("setInstitutionRootName");
        delegation.setType(DelegationType.PT);
        delegation.setCreatedAt(OffsetDateTime.now().minusDays(2));
        delegation.setUpdatedAt(OffsetDateTime.now());
        delegation.setInstitutionType(InstitutionType.PT);
        delegation.setStatus("ACTIVE");
        delegation.setTaxCode("taxCode");
        return delegation;
    }

    private GetDelegationParameters dummyDelegationParametersTo() {
        return GetDelegationParameters.builder()
                .to("to")
                .productId("product-io")
                .taxCode("taxCode")
                .search("name")
                .mode(GetDelegationsMode.FULL.name())
                .order(Order.ASC.name())
                .page(0)
                .size(1000)
                .build();
    }

}