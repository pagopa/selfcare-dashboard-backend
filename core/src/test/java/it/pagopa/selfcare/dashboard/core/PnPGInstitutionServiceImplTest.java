package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdatePnPGInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.core.PnPGInstitutionServiceImpl.REQUIRED_INSTITUTION_MESSAGE;
import static it.pagopa.selfcare.dashboard.core.PnPGInstitutionServiceImpl.REQUIRED_UPDATE_RESOURCE_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PnPGInstitutionServiceImpl.class,
        CoreTestConfig.class
})
@TestPropertySource(properties = {
        "USER_STATES_FILTER=ACTIVE,SUSPENDED"
})
class PnPGInstitutionServiceImplTest {

    @MockBean
    private MsCoreConnector msCoreConnectorMock;

    @Autowired
    private PnPGInstitutionServiceImpl pnPGInstitutionService;

    @Captor
    private ArgumentCaptor<CreateUserDto> createUserDtoCaptor;


    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getInstitutions() {
        // given
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        when(msCoreConnectorMock.getOnBoardedInstitutions())
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = pnPGInstitutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verify(msCoreConnectorMock, times(1))
                .getOnBoardedInstitutions();
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void getInstitutionProducts_notNull() throws Exception {
        // given
        String institutionId = "institutionId";
        List<PartyProduct> products = List.of(mockInstance(new PartyProduct()));
        when(msCoreConnectorMock.getInstitutionProducts(any()))
                .thenReturn(products);
        // when
        List<PartyProduct> results = pnPGInstitutionService.getInstitutionProducts(institutionId);
        // then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        verify(msCoreConnectorMock, times(1))
                .getInstitutionProducts(institutionId);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void getInstitutionProducts_empty() throws Exception {
        // given
        String institutionId = "institutionId";
        when(msCoreConnectorMock.getInstitutionProducts(any()))
                .thenReturn(Collections.emptyList());
        // when
        List<PartyProduct> results = pnPGInstitutionService.getInstitutionProducts(institutionId);
        // then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(msCoreConnectorMock, times(1))
                .getInstitutionProducts(institutionId);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void updateInstitutionDescription() {
        // given
        String institutionId = "setId";
        UpdatePnPGInstitutionResource resource = mockInstance(new UpdatePnPGInstitutionResource());
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.updateInstitutionDescription(anyString(), any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = pnPGInstitutionService.updateInstitutionDescription(institutionId, resource);
        // then
        assertEquals(institution.getId(), institutionId);
        assertEquals(institution.getDescription(), resource.getDescription());
        assertEquals(institution.getDigitalAddress(), resource.getDigitalAddress());
        verify(msCoreConnectorMock, times(1))
                .updateInstitutionDescription(institutionId, resource);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void updateInstitutionDescription_hasNullInstitutionId() {
        // given
        String institutionId = null;
        UpdatePnPGInstitutionResource resource = mockInstance(new UpdatePnPGInstitutionResource());
        // when
        Executable executable = () -> pnPGInstitutionService.updateInstitutionDescription(institutionId, resource);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void updateInstitutionDescription_hasNullDescription() {
        // given
        String institutionId = "institutionId";
        UpdatePnPGInstitutionResource resource = null;
        // when
        Executable executable = () -> pnPGInstitutionService.updateInstitutionDescription(institutionId, resource);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_UPDATE_RESOURCE_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

}