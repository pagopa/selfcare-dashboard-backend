package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomy;
import it.pagopa.selfcare.dashboard.connector.model.institution.GeographicTaxonomyList;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InstitutionServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private InstitutionServiceImpl institutionService;
    @Mock
    private MsCoreConnector msCoreConnectorMock;
    @Mock
    private ProductsConnector productsConnectorMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getInstitutionById() throws IOException {

        String institutionId = "institutionId";

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        Institution result = institutionService.getInstitutionById(institutionId);
        Assertions.assertEquals(institution, result);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitution(institutionId);
    }

    @Test
    void getInstitutionByIdEmptyInstitution() {

        String institutionId = "institutionId";

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(null);

        Institution result = institutionService.getInstitutionById(institutionId);
        Assertions.assertNull(result);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitution(institutionId);
    }

    @Test
    void updateInstitutionGeographicTaxonomy() {

        String institutionId = "institutionId";
        GeographicTaxonomy geographicTaxonomy = new GeographicTaxonomy();
        geographicTaxonomy.setCode("testCode1");
        geographicTaxonomy.setDesc("testDesc1");

        GeographicTaxonomyList geographicTaxonomyList = new GeographicTaxonomyList();
        geographicTaxonomyList.setGeographicTaxonomyList(List.of(geographicTaxonomy));

        Mockito.doNothing()
                .when(msCoreConnectorMock).updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomyList);

        institutionService.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomyList);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomyList);
    }

    @Test
    void updateInstitutionGeographicTaxonomyWithoutInstitutionId() {

        GeographicTaxonomyList geographicTaxonomies = new GeographicTaxonomyList();
        Assertions.assertThrows(IllegalArgumentException.class, () -> institutionService.updateInstitutionGeographicTaxonomy(null, geographicTaxonomies));
    }

    @Test
    void updateInstitutionGeographicTaxonomyWithoutGeographicTaxonomies() {

        String institutionId = "institutionId";
        Assertions.assertThrows(IllegalArgumentException.class, () -> institutionService.updateInstitutionGeographicTaxonomy(institutionId, null));
    }

    @Test
    void getProductsTree() throws IOException {

        ClassPathResource resource = new ClassPathResource("expectations/ProductTree.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        List<ProductTree> productTrees = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(productsConnectorMock.getProductsTree()).thenReturn(productTrees);

        List<ProductTree> result = institutionService.getProductsTree();
        Assertions.assertEquals(productTrees, result);
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductsTree();

    }

    @Test
    void getProductsTreeEmptyProduct() {

        when(productsConnectorMock.getProductsTree()).thenReturn(Collections.emptyList());

        List<ProductTree> result = institutionService.getProductsTree();
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void updateInstitutionDescription() throws IOException {

        String institutionId = "institutionId";
        UpdateInstitutionResource updateInstitutionResource = new UpdateInstitutionResource();
        updateInstitutionResource.setDescription("updatedDescription");
        updateInstitutionResource.setDigitalAddress("updatedDigitalAddress");

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreConnectorMock.updateInstitutionDescription(institutionId, updateInstitutionResource)).thenReturn(institution);

        Institution result = institutionService.updateInstitutionDescription(institutionId, updateInstitutionResource);

        Assertions.assertEquals(institution, result);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).updateInstitutionDescription(institutionId, updateInstitutionResource);

    }

    @Test
    void updateInstitutionDescriptionWithoutInstitutionId() {

        UpdateInstitutionResource updateInstitutionResource = new UpdateInstitutionResource();
        updateInstitutionResource.setDescription("description");
        updateInstitutionResource.setDigitalAddress("digitalAddress");

        Assertions.assertThrows(IllegalArgumentException.class, () -> institutionService.updateInstitutionDescription(null, updateInstitutionResource));
    }

    @Test
    void updateInstitutionDescriptionWithoutUpdateInstitutionResource() {

        String institutionId = "institutionId";

        Assertions.assertThrows(IllegalArgumentException.class, () -> institutionService.updateInstitutionDescription(institutionId, null));
    }

    @Test
    void updateInstitutionDescriptionEmptyInstitution() {

        String institutionId = "institutionId";
        UpdateInstitutionResource updateInstitutionResource = new UpdateInstitutionResource();
        updateInstitutionResource.setDescription("updatedDescription");
        updateInstitutionResource.setDigitalAddress("updatedDigitalAddress");

        when(msCoreConnectorMock.updateInstitutionDescription(institutionId, updateInstitutionResource)).thenReturn(new Institution());

        Institution result = institutionService.updateInstitutionDescription(institutionId, updateInstitutionResource);
        Assertions.assertEquals(new Institution(), result);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).updateInstitutionDescription(institutionId, updateInstitutionResource);
    }

    @Test
    void findInstitutionById() throws IOException {

        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(OPERATOR, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("123e4567-e89b-12d3-a456-426614174000", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        institutionService.findInstitutionById(institutionId);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitution(institutionId);
    }

    @Test
    void findInstitutionByIdWithLimitation() throws IOException {

        String institutionId = "123e4567-e89b-12d3-a456-426614174000";
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId2");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("123e4567-e89b-12d3-a456-426614174000", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        institutionService.findInstitutionById(institutionId);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitution(institutionId);
    }

    @Test
    void findInstitutionByIdWithoutMatch() throws IOException {

        String institutionId = "institutionId";
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("noMatchInstitutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ClassPathResource resource = new ClassPathResource("expectations/Institution.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Institution institution = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        Institution result = institutionService.findInstitutionById(institutionId);
        Assertions.assertEquals(institution, result);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitution(institutionId);
    }

    @Test
    void findInstitutionByIdWithoutInstitutionId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> institutionService.findInstitutionById(null));
    }

    @Test
    void findInstitutionByIdEmpty() {

        String institutionId = "institutionId";

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(null);

        Institution result = institutionService.findInstitutionById(institutionId);
        Assertions.assertNull(result);
        Mockito.verify(msCoreConnectorMock, Mockito.times(1)).getInstitution(institutionId);
    }
}