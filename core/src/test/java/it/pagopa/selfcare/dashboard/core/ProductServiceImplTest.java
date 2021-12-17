package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
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
class ProductServiceImplTest {

    private static final Set<String> PARTY_ROLE_WHITE_LIST = Set.of("SUB_DELEGATE", "OPERATORS");

    @Mock
    private ProductsConnector productsConnectorMock;

    @InjectMocks
    private ProductServiceImpl productService;


    @Test
    void getProductRoles_nullProductId() {
        // given
        String productId = null;
        // when
        Executable executable = () -> productService.getProductRoles(productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock);
    }


    @Test
    void getProductRoles() {
        // given
        String productId = "productId";
        Map<String, List<String>> productRoleMappings = Map.of(
                "MANAGER", List.of("role-1"),
                "DELEGATE", List.of("role-2"),
                "SUB_DELEGATE", List.of("role-3"),
                "OPERATORS", List.of("role-4", "role-5")
        );
        Mockito.when(productsConnectorMock.getProductRoleMappings(Mockito.any()))
                .thenReturn(productRoleMappings);
        // when
        Collection<String> productRoles = productService.getProductRoles(productId);
        // then
        Assertions.assertNotNull(productRoles);
        Assertions.assertFalse(productRoles.isEmpty());
        TreeSet<String> expectedRoles = new TreeSet<>();
        expectedRoles.addAll(productRoleMappings.get("SUB_DELEGATE"));
        expectedRoles.addAll(productRoleMappings.get("OPERATORS"));
        Assertions.assertIterableEquals(expectedRoles, new TreeSet<>(productRoles));
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductRoleMappings(productId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
    }

}