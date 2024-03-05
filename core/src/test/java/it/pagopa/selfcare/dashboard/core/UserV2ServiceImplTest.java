package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class UserV2ServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserV2ServiceImpl userService;

    @Mock
    private MsCoreConnector msCoreConnectorMock;

    @Mock
    private UserApiConnector userApiConnector;

    @Mock
    private UserGroupService userGroupService;

    @Test
    void getInstitutions() {
        // given
        String userId = "userId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();

        when(userApiConnector.getUserProducts(userId)).thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = userService.getInstitutions(userId);

        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verifyNoMoreInteractions(msCoreConnectorMock);
    }


    @Test
    void suspend() {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        userService.suspendUserProduct(userId, institutionid, productId);
        Mockito.verify(userApiConnector, Mockito.times(1))
                .suspendUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector);
    }


    @Test
    void activate() {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        // when
        userService.activateUserProduct(userId, institutionid, productId);
        // then
        Mockito.verify(userApiConnector, Mockito.times(1))
                .activateUserProduct(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void delete() {
        //given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        //when
        userService.deleteUserProduct(userId, institutionid, productId);
        //then
        Mockito.verify(userApiConnector, Mockito.times(1))
                .deleteUserProduct(userId, institutionid, productId);
        Mockito.verify(userGroupService, Mockito.times(1))
                .deleteMembersByUserId(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector, userGroupService);
    }

    @Test
    void getById(){
        //given
        String userId = "userId";
        User user = mockInstance(new User());
        when(userApiConnector.getUserById(anyString())).thenReturn(user);
        //when
        User result = userService.getUserById(userId);
        //then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userApiConnector, times(1)).getUserById(userId);
    }

    @Test
    void searchByFiscalCode(){
        String fiscalCode = "fiscalCode";
        User user = mockInstance(new User());
        when(userApiConnector.searchByFiscalCode(anyString())).thenReturn(user);
        //when
        User result = userService.searchUserByFiscalCode(fiscalCode);
        //then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userApiConnector, times(1)).searchByFiscalCode(fiscalCode);
    }
}