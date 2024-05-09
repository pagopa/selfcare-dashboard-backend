package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.exception.SupportException;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportRequest;
import it.pagopa.selfcare.dashboard.connector.model.support.SupportResponse;
import it.pagopa.selfcare.dashboard.connector.model.support.UserField;
import it.pagopa.selfcare.dashboard.connector.model.user.CertifiedField;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SupportServiceImplTest {

    @Mock
    private UserRegistryConnector userRegistryConnector;

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequest(){
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "http://www.test.it", "test-organization", "redirectUrl", null);
        SupportResponse response = supportServiceImpl.sendRequest(this.dummySupportRequest());
        assertNotNull(response);
        assertNotNull(response.getJwt());
        assertNotNull(response.getRedirectUrl());
        assertEquals("http", response.getRedirectUrl().substring(0, 4));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestWithUserId(){
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "http://www.test.it", "test-organization", "redirectUrl", userRegistryConnector);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setUserId("userId");
        supportRequest.setInstitutionId("institutionId");
        User user = new User();
        user.setFiscalCode("fiscalCode");
        CertifiedField<String> certifiedField = new CertifiedField<>();
        certifiedField.setValue("name");
        user.setName(certifiedField);
        user.setFamilyName(certifiedField);
        when(userRegistryConnector.getUserByInternalId(anyString(), any())).thenReturn(user);
        SupportResponse response = supportServiceImpl.sendRequest(supportRequest);
        assertNotNull(response);
        assertNotNull(response.getJwt());
        assertNotNull(response.getRedirectUrl());
        assertEquals("http", response.getRedirectUrl().substring(0, 4));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestUserNotFound(){
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "", "test-organization", "redirectUrl", null);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setUserId("null");
        Executable executable = () -> supportServiceImpl.sendRequest(supportRequest);
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        Assertions.assertEquals("User with id null not found", e.getMessage());
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testSendRequestWithProductId() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("w90kAW1FIIJaMuWbKGyd8GfDkv45tVPiyYvrdLADsK2ANX26", "http://www.test.it", "", "redirectUrl", null);
        SupportRequest supportRequest = this.dummySupportRequest();
        supportRequest.setProductId("prodottoDiTest");
        SupportResponse response = supportServiceImpl.sendRequest(supportRequest);
        assertNotNull(response);
        assertNotNull(response.getJwt());
        assertNotNull(response.getRedirectUrl());
        assertEquals("http", response.getRedirectUrl().substring(0, 4));
        assertTrue(response.getRedirectUrl().contains(supportRequest.getProductId()));
    }

    /**
     * Method under test: {@link SupportServiceImpl#sendRequest(SupportRequest)}
     */
    @Test
    void testRequestWithMalformedEmptyKey() {
        SupportServiceImpl supportServiceImpl = new SupportServiceImpl("", "", "test", "redirectUrl", null);
        SupportException exception = assertThrows(SupportException.class, () -> supportServiceImpl.sendRequest(this.dummySupportRequest()));
        assertEquals("secret key byte array cannot be null or empty.", exception.getMessage());
    }
    
    private SupportRequest dummySupportRequest() {
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setName("Mario Rossi");
        supportRequest.setEmail("test@gmail.com");
        UserField userField = new UserField();
        userField.setAux_data("PPL89M");
        supportRequest.setUserFields(userField);
        return supportRequest;
    }

}
