package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RelationshipServiceImplTest {

    @Mock
    private PartyConnector partyConnectorMock;

    @InjectMocks
    private RelationshipServiceImpl relationshipService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserGroupConnector groupConnectorMock;


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
        Mockito.verifyNoInteractions(notificationService);
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
        Mockito.verify(notificationService, Mockito.times(1))
                .sendSuspendedUserNotification(relationshipId);
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
        Mockito.verifyNoInteractions(notificationService);
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
        Mockito.verify(notificationService, Mockito.times(1))
                .sendActivatedUserNotification(relationshipId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void delete_notEmptyRelations() {
        // given
        String relationshipId = "relationshipId";
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo());
        userInfoMock.setInstitutionId(institutionId);
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(Optional.of(productId));
        filter.setUserId(Optional.of(userInfoMock.getId()));
        Mockito.when(partyConnectorMock.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        String id4 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");
        UserInfo userInfoMock3 = TestUtils.mockInstance(new UserInfo(), 3, "setId");
        UserInfo userInfoMock4 = TestUtils.mockInstance(new UserInfo(), 4, "setId");

        userInfoMock1.setId(id1);
        userInfoMock2.setId(id2);
        userInfoMock3.setId(id3);
        userInfoMock4.setId(id4);

        List<UserInfo> members = List.of(userInfoMock1, userInfoMock2, userInfoMock3, userInfoMock4);

        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(members);
        // when
        relationshipService.delete(relationshipId);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .delete(relationshipId);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, filter);

        Mockito.verify(notificationService, Mockito.times(1))
                .sendDeletedUserNotification(relationshipId);
//        Mockito.verify(groupConnectorMock, Mockito.times(1))
//                .deleteMembers(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verifyNoInteractions(groupConnectorMock);
        Mockito.verifyNoMoreInteractions(partyConnectorMock, groupConnectorMock, notificationService);
    }

    @Test
    void delete_emptyRelationships() {
        // given
        String relationshipId = "relationshipId";
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo());
        userInfoMock.setInstitutionId(institutionId);
        ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo());
        productInfoMock.setId(productId);
        Map<String, ProductInfo> products = new HashMap<>();
        products.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(products);
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setProductId(Optional.of(productId));
        filter.setUserId(Optional.of(userInfoMock.getId()));
        Mockito.when(partyConnectorMock.getUser(Mockito.anyString()))
                .thenReturn(userInfoMock);

        Mockito.when(partyConnectorMock.getUsers(Mockito.anyString(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        relationshipService.delete(relationshipId);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .delete(relationshipId);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUser(relationshipId);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, filter);

        Mockito.verify(notificationService, Mockito.times(1))
                .sendDeletedUserNotification(relationshipId);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .deleteMembers(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(partyConnectorMock, groupConnectorMock, notificationService);
    }

    @Test
    void delete_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> relationshipService.delete(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Relationship id is required", e.getMessage());
        Mockito.verifyNoInteractions(notificationService);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }
}