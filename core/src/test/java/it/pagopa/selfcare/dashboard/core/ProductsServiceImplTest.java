package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static it.pagopa.selfcare.commons.base.security.Authority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.Authority.LIMITED;

@ExtendWith(MockitoExtension.class)
class ProductsServiceImplTest {

    @InjectMocks
    private ProductsServiceImpl productsService;
    @Mock
    private ProductsConnector productsConnectorMock;
    @Mock
    private PartyConnector partyConnectorMock;


    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void getProducts_emptyProducts() {
        //given
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(Collections.emptyList());
        //when
        List<Product> products = productsService.getProducts();
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void getProducts_limitedWithEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product product = TestUtils.mockInstance(new Product());
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(product));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(LIMITED, "", "");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(Collections.singleton(productGrantedAuthority))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = productsService.getProducts();
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getProducts_adminWithEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product product = TestUtils.mockInstance(new Product());
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(product));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(ADMIN, "", product.getCode());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(Collections.singleton(productGrantedAuthority))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = productsService.getProducts();
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertFalse(products.get(0).isActive());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getProducts_operatorWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product p1 = TestUtils.mockInstance(new Product(), 1);
        Product p2 = TestUtils.mockInstance(new Product(), 2);
        Product p3 = TestUtils.mockInstance(new Product(), 3);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(p1, p2, p3));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(p1.getCode(), p3.getCode()));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(LIMITED, "", p2.getCode());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(LIMITED, "", p3.getCode());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(List.of(productGrantedAuthority2, productGrantedAuthority3))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = productsService.getProducts();
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertEquals(p3.getId(), products.get(0).getId());
        Assertions.assertTrue(products.get(0).isActive());
        Assertions.assertTrue(products.get(0).isAuthorized());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getProducts_adminWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product p1 = TestUtils.mockInstance(new Product(), 1);
        Product p2 = TestUtils.mockInstance(new Product(), 2);
        Product p3 = TestUtils.mockInstance(new Product(), 3);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(p1, p2, p3));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(p1.getCode(), p3.getCode()));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(ADMIN, "", p2.getCode());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(ADMIN, "", p3.getCode());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(List.of(productGrantedAuthority2, productGrantedAuthority3))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = productsService.getProducts();
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(3, products.size());
        Set<String> expectedActiveProducts = Set.of(p3.getId(), p1.getId());
        Set<String> expectedAuthorizedProducts = Set.of(p3.getId(), p2.getId());
        products.forEach(product -> {
            Assertions.assertEquals(expectedActiveProducts.contains(product.getId()), product.isActive());
            Assertions.assertEquals(expectedAuthorizedProducts.contains(product.getId()), product.isAuthorized());
        });
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(Mockito.eq(institutionId));
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

}