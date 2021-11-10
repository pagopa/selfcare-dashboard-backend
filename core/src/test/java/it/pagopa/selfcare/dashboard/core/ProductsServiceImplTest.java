package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class ProductsServiceImplTest {

    @InjectMocks
    private ProductsServiceImpl productsService;
    @Mock
    private ProductsConnector productsConnectorMock;

    @Test
    void getProducts() {//FIXME
        //given
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("TECH_REF")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //given and when
        productsService.getProducts();
        //then
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
    }
}