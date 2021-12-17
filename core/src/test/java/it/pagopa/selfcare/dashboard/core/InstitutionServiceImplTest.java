package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthenticationDetails;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

    @Mock
    private PartyConnector partyConnectorMock;

    @Mock
    private ProductsConnector productsConnectorMock;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    @Captor
    private ArgumentCaptor<CreateUserDto> createUserDtoCaptor;


    @BeforeEach
    void beforeEach() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        Mockito.when(partyConnectorMock.getInstitution(Mockito.any()))
                .thenReturn(expectedInstitutionInfo);
        // when
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        // then
        assertSame(expectedInstitutionInfo, institutionInfo);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void getInstitutions() {
        // given
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        Mockito.when(partyConnectorMock.getInstitutions())
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getInstitutions();
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }


    @Test
    void getInstitutionProducts_emptyProducts() {
        //given
        String institutionId = "institutionId";
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(Collections.emptyList());
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void getInstitutionProducts_limitedWithEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product product = TestUtils.mockInstance(new Product());
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(product));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(LIMITED, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(productGrantedAuthority))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProducts_adminWithEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product product = TestUtils.mockInstance(new Product());
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(product));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(ADMIN, "productRole", product.getId());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(productGrantedAuthority))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertFalse(products.get(0).isActive());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProducts_operatorWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product p1 = TestUtils.mockInstance(new Product(), 1);
        Product p2 = TestUtils.mockInstance(new Product(), 2);
        Product p3 = TestUtils.mockInstance(new Product(), 3);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(p1, p2, p3));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(p1.getId(), p3.getId()));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(LIMITED, "productRole2", p2.getId());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(LIMITED, "productRole3", p3.getId());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority2, productGrantedAuthority3))));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertEquals(p3.getId(), products.get(0).getId());
        Assertions.assertTrue(products.get(0).isActive());
        Assertions.assertTrue(products.get(0).isAuthorized());
        Assertions.assertEquals(LIMITED.name(), products.get(0).getUserRole());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getInstitutionProducts_adminWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product p1 = TestUtils.mockInstance(new Product(), 1);
        Product p2 = TestUtils.mockInstance(new Product(), 2);
        Product p3 = TestUtils.mockInstance(new Product(), 3);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(p1, p2, p3));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(p1.getId(), p3.getId()));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(ADMIN, "productRole2", p2.getId());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(ADMIN, "productRole3", p3.getId());
        SelfCareGrantedAuthority selfCareGrantedAuthority = new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority2, productGrantedAuthority3));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(selfCareGrantedAuthority));
        authentication.setDetails(new SelfCareAuthenticationDetails(institutionId));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(3, products.size());
        Set<String> expectedActiveProducts = Set.of(p3.getId(), p1.getId());
        Set<String> expectedAuthorizedProducts = Set.of(p3.getId(), p2.getId());
        products.forEach(product -> {
            Assertions.assertEquals(expectedActiveProducts.contains(product.getId()), product.isActive());
            Assertions.assertEquals(expectedAuthorizedProducts.contains(product.getId()), product.isAuthorized());
            if (selfCareGrantedAuthority.getRoleOnProducts().containsKey(product.getId())) {
                Assertions.assertEquals(selfCareGrantedAuthority.getRoleOnProducts().get(product.getId()).getSelfCareAuthority().name(), product.getUserRole());
            } else {
                Assertions.assertNull(product.getUserRole());
            }
        });
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        Optional<SelfCareAuthority> role = Optional.empty();
        Set<String> productIds = null;
        // when
        Executable executable = () -> {
            institutionService.getUsers(institutionId, role, productIds);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getUsers_nullRole() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = null;
        Set<String> productIds = null;
        // when
        Executable executable = () -> institutionService.getUsers(institutionId, role, productIds);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional role object is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void getUsers_nullProductIds() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Set<String> productIds = null;
        // when
        institutionService.getUsers(institutionId, role, productIds);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, role, Optional.empty());
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
        Mockito.verifyNoInteractions(productsConnectorMock);
    }


    @Test
    void getUsers_notNullProductIds() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Set<String> productIds = Set.of("prod-1");
        // when
        institutionService.getUsers(institutionId, role, productIds);
        // then
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, role, Optional.of(productIds));
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
        Mockito.verifyNoInteractions(productsConnectorMock);
    }


    @Test
    void getUsers_fillProductTitle() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setId");
        ProductInfo productInfo1 = new ProductInfo();
        String productId1 = "prod-1";
        String productId2 = "prod-2";
        productInfo1.setId(productId1);
        ProductInfo productInfo2 = new ProductInfo();
        productInfo2.setId(productId2);
        userInfoMock.setProducts(List.of(productInfo1, productInfo2));
        Mockito.when(partyConnectorMock.getUsers(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock));
        Product product1 = TestUtils.mockInstance(new Product(), 1, "setId");
        product1.setId(productId1);
        Product product2 = TestUtils.mockInstance(new Product(), 2, "setId");
        product2.setId(productId2);
        Map<String, Product> idToProductMap = Map.of(productId1, product1, productId2, product2);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(new ArrayList<>(idToProductMap.values()));
        // when
        Collection<UserInfo> userInfos = institutionService.getUsers(institutionId, role);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(2, userInfo.getProducts().size());
        userInfo.getProducts().forEach(productInfo ->
                Assertions.assertEquals(idToProductMap.get(productInfo.getId()).getTitle(), productInfo.getTitle()));
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, role, Optional.empty());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProducts();
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }


    @Test
    void createUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void createUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void createUsers_nullUser() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        CreateUserDto createUserDto = null;
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An User is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }


    @Test
    void createUsers_invalidRole() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setProductRole("invalid-productRole");
        Mockito.when(productsConnectorMock.getProductRoleMappings(Mockito.anyString()))
                .thenReturn(Map.of("partyRole", List.of("productRole")));
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        InvalidProductRoleException e = assertThrows(InvalidProductRoleException.class, executable);
        Assertions.assertEquals(String.format("Product role '%s' is not valid", createUserDto.getProductRole()), e.getMessage());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductRoleMappings(productId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }


    @Test
    void createUsers() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String partyRole = "partyRole";
        String productRole = "productRole";
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setProductRole", "setPartyRole");
        createUserDto.setProductRole(productRole);
        Mockito.when(productsConnectorMock.getProductRoleMappings(Mockito.anyString()))
                .thenReturn(Map.of(partyRole, List.of(productRole)));
        // when
        institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductRoleMappings(productId);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), createUserDtoCaptor.capture());
        Assertions.assertEquals(partyRole, createUserDtoCaptor.getValue().getPartyRole());
        TestUtils.reflectionEqualsByName(createUserDtoCaptor.getValue(), createUserDto);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

}