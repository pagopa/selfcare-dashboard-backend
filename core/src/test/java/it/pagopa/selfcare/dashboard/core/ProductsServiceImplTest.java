package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.rest.client.ProductsRestClient;
import it.pagopa.selfcare.dashboard.core.ProductsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductsServiceImplTest {

    @InjectMocks
    private ProductsServiceImpl productsService;
    @Mock
    private ProductsRestClient restClient;

    @Test
    void getProducts() {
        //given and when
        productsService.getProducts();
        //then
        Mockito.verify(restClient, Mockito.times(1)).getProducts();
    }
}