package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductTree;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.core.InstitutionServiceImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InstitutionServiceImpl.class,
        CoreTestConfig.class
})
@TestPropertySource(properties = {
        "USER_STATES_FILTER=ACTIVE,SUSPENDED"
})
class InstitutionServiceImplTest {

    @MockBean
    private MsCoreConnector msCoreConnectorMock;

    @MockBean
    private UserRegistryConnector userRegistryConnector;

    @MockBean
    private ProductsConnector productsConnectorMock;

    @Autowired
    private InstitutionServiceImpl institutionService;

    @Captor
    private ArgumentCaptor<CreateUserDto> createUserDtoCaptor;


    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getInstitutionById() {
        // Given
        String institutionId = "institutionId";
        Institution institution = new Institution();
        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        // When
        Institution result = institutionService.getInstitutionById(institutionId);

        // Then
        verify(msCoreConnectorMock, times(1)).getInstitution(institutionId);
        assertEquals(institution, result);
    }

    @Test
    void updateInstitutionGeographicTaxonomy() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomies = new GeographicTaxonomyList();
        geographicTaxonomies.setGeographicTaxonomyList(List.of(mockInstance(new GeographicTaxonomy())));
        Mockito.doNothing()
                .when(msCoreConnectorMock).updateInstitutionGeographicTaxonomy(anyString(), any());
        // when
        institutionService.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        // then
        verify(msCoreConnectorMock, times(1))
                .updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void updateInstitutionGeographicTaxonomy_hasNullInstitutionId() {
        // given
        String institutionId = null;
        GeographicTaxonomyList geographicTaxonomies = new GeographicTaxonomyList();
        // when
        Executable executable = () -> institutionService.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void updateInstitutionGeographicTaxonomy_hasNullGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomies = null;
        // when
        Executable executable = () -> institutionService.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomies);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GEOGRAPHIC_TAXONOMIES, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void getGeographicTaxonomyList() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        when(msCoreConnectorMock.getGeographicTaxonomyList(anyString()))
                .thenReturn(institutionMock.getGeographicTaxonomies());
        // when
        List<GeographicTaxonomy> result = institutionService.getGeographicTaxonomyList(institutionId);
        // then
        assertNotNull(result);
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getCode(), result.get(0).getCode());
        assertEquals(institutionMock.getGeographicTaxonomies().get(0).getDesc(), result.get(0).getDesc());
        verify(msCoreConnectorMock, times(1))
                .getGeographicTaxonomyList(institutionId);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void getGeographicTaxonomyList_hasNullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> institutionService.getGeographicTaxonomyList(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void getProductsTree_emptyProducts() {
        when(productsConnectorMock.getProductsTree())
                .thenReturn(Collections.emptyList());
        //when
        List<ProductTree> products = institutionService.getProductsTree();
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        verify(productsConnectorMock, times(1)).getProductsTree();
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void getProductsTree() {
        ProductTree product = mock(ProductTree.class);
        List<ProductTree> productList = List.of(product);
        when(productsConnectorMock.getProductsTree())
                .thenReturn(productList);
        //when
        List<ProductTree> products = institutionService.getProductsTree();
        //then
        Assertions.assertNotNull(products);
        assertEquals(1, products.size());
        verify(productsConnectorMock, times(1)).getProductsTree();
        verifyNoMoreInteractions(productsConnectorMock);
        verifyNoInteractions(msCoreConnectorMock);
    }


    @Test
    void updateInstitutionDescription() {
        // given
        String institutionId = "setId";
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.updateInstitutionDescription(anyString(), any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = institutionService.updateInstitutionDescription(institutionId, resource);
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
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        // when
        Executable executable = () -> institutionService.updateInstitutionDescription(institutionId, resource);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void updateInstitutionDescription_hasNullDescription() {
        // given
        String institutionId = "institutionId";
        UpdateInstitutionResource resource = null;
        // when
        Executable executable = () -> institutionService.updateInstitutionDescription(institutionId, resource);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_UPDATE_RESOURCE_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void findInstitutionByIdTest2() {
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(MANAGER, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Institution institution = new Institution();
        institution.setExternalId("externalId");
        institution.setDescription("description");
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId("productId");
        institution.setOnboarding(Collections.singletonList(onboardedProduct));
        when(msCoreConnectorMock.getInstitution("institutionId")).thenReturn(institution);
        Institution institutionResponse = institutionService.findInstitutionById("institutionId");
        Assertions.assertEquals("description", institutionResponse.getDescription());
        Assertions.assertEquals("externalId", institutionResponse.getExternalId());
    }

    @Test
    void findInstitutionByIdTest() {
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(OPERATOR, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Institution institution = new Institution();
        institution.setExternalId("externalId");
        institution.setDescription("description");
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId("productId");
        institution.setOnboarding(Collections.singletonList(onboardedProduct));
        when(msCoreConnectorMock.getInstitution("institutionId")).thenReturn(institution);
        Institution institutionResponse = institutionService.findInstitutionById("institutionId");
        Assertions.assertEquals("description", institutionResponse.getDescription());
        Assertions.assertEquals("externalId", institutionResponse.getExternalId());
    }
}