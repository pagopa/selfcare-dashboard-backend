package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.model.delegation.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegationServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private DelegationServiceImpl delegationServiceImpl;
    @Mock
    private MsCoreConnector msCoreConnector;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void testCreateDelegation() throws IOException {

        ClassPathResource pathResource = new ClassPathResource("expectations/DelegationRequest.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        DelegationRequest delegation = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        DelegationId delegationId = new DelegationId();
        delegationId.setId("id");
        when(msCoreConnector.createDelegation(delegation)).thenReturn(delegationId);

        DelegationId response = delegationServiceImpl.createDelegation(delegation);
        Assertions.assertEquals(delegation.getId(), response.getId());
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .createDelegation(delegation);
    }


    @Test
    void testCreateDelegationPagoPa() throws IOException {

        ClassPathResource pathResource = new ClassPathResource("expectations/DelegationRequestPagoPa.json");
        byte[] resourceStream = Files.readAllBytes(pathResource.getFile().toPath());
        DelegationRequest delegation = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        DelegationRequest delegationTaxCode = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        ClassPathResource pathResourceInstitution = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStreamInstitution = Files.readAllBytes(pathResourceInstitution.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStreamInstitution, new TypeReference<>() {
        });

        DelegationId delegationId = new DelegationId();
        delegationId.setId("id");
        when(msCoreConnector.getInstitutionsFromTaxCode(delegationTaxCode.getTo(), null, null, null)).thenReturn(List.of(institution));
        when(msCoreConnector.createDelegation(delegation)).thenReturn(delegationId);

        DelegationId response = delegationServiceImpl.createDelegation(delegation);
        Assertions.assertEquals(delegation.getId(), response.getId());
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .getInstitutionsFromTaxCode(delegationTaxCode.getTo(), null, null, null);
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .createDelegation(delegation);
    }

    @Test
    void getDelegations() throws IOException {

        ClassPathResource pathExpectation = new ClassPathResource("expectations/Delegation.json");
        byte[] ExpectationStream = Files.readAllBytes(pathExpectation.getFile().toPath());
        List<Delegation> delegations = objectMapper.readValue(ExpectationStream, new TypeReference<>() {
        });

        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .productId("setProductId")
                .taxCode("taxCode")
                .search("name")
                .order("ASC")
                .page(0)
                .size(1000)
                .build();

        when(msCoreConnector.getDelegations(delegationParameters)).thenReturn(delegations);

        List<Delegation> response = delegationServiceImpl.getDelegations(delegationParameters);

        Assertions.assertEquals(delegations, response);
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .getDelegations(delegationParameters);
    }

    @Test
    void getDelegationV2() throws IOException {

        ClassPathResource pathExpectation = new ClassPathResource("expectations/DelegationWithPagination.json");
        byte[] ExpectationStream = Files.readAllBytes(pathExpectation.getFile().toPath());
        DelegationWithPagination delegationWithPagination = objectMapper.readValue(ExpectationStream, new TypeReference<>() {
        });

        GetDelegationParameters delegationParameters = GetDelegationParameters.builder()
                .productId("setProductId")
                .taxCode("taxCode")
                .search("name")
                .order("ASC")
                .page(0)
                .size(1000)
                .build();

        when(msCoreConnector.getDelegationsV2(delegationParameters)).thenReturn(delegationWithPagination);

        DelegationWithPagination response = delegationServiceImpl.getDelegationsV2(delegationParameters);

        Assertions.assertEquals(delegationWithPagination, response);
        Mockito.verify(msCoreConnector, Mockito.times(1))
                .getDelegationsV2(delegationParameters);
    }

}