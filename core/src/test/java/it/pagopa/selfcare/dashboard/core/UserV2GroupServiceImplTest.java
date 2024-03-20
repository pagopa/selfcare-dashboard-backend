package it.pagopa.selfcare.dashboard.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserGroupConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInstitution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

@ContextConfiguration(classes = {UserV2GroupServiceImpl.class})
@ExtendWith(SpringExtension.class)
class UserV2GroupServiceImplTest {
    @MockBean
    private UserApiConnector userApiConnector;

    @MockBean
    private UserGroupConnector userGroupConnector;

    @Autowired
    private UserV2GroupServiceImpl userV2GroupServiceImpl;

    /**
     * Method under test:
     * {@link UserV2GroupServiceImpl#deleteMembersByUserId(String, String, String)}
     */
    @Test
    void testDeleteMembersByUserIdUserNotFound() {
        // Arrange
        when(userApiConnector.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(Collections.emptyList());

        // Act
        userV2GroupServiceImpl.deleteMembersByUserId("userId", "institutionId", "prod-io");

        // Assert that nothing has changed
        verify(userApiConnector).retrieveFilteredUser("userId", "institutionId", "prod-io");
    }

    /**
     * Method under test:
     * {@link UserV2GroupServiceImpl#deleteMembersByUserId(String, String, String)}
     */
    @Test
    void testDeleteMembersByUserIdUserFound() {
        // Arrange
        when(userApiConnector.retrieveFilteredUser("userId", "institutionId", "prod-io")).thenReturn(List.of(Mockito.mock(UserInstitution.class)));

        // Act and Assert
        userV2GroupServiceImpl.deleteMembersByUserId("userId", "institutionId", "prod-io");
        verify(userApiConnector).retrieveFilteredUser("userId","institutionId","prod-io");
        verifyNoInteractions(userGroupConnector);
    }
}
