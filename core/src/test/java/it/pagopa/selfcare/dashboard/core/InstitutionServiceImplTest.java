package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductRoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductStatus;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    void getInstitutionProducts_GrantedAuthorityOnDifferentInstId() {
        //given
        String institutionId = "institutionId";
        Product product = TestUtils.mockInstance(new Product());
        List<Product> productList = List.of(product);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(productList);
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(LIMITED, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId2", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertIterableEquals(productList, products);
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
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertEquals(ProductStatus.INACTIVE, products.get(0).getStatus());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProducts();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_limitedWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        Product p1 = TestUtils.mockInstance(new Product(), 1);
        Product p2 = TestUtils.mockInstance(new Product(), 2);
        Product p3 = TestUtils.mockInstance(new Product(), 3);
        Product p4 = TestUtils.mockInstance(new Product(), 4);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(p1, p2, p3, p4));
        PartyProduct pp1 = new PartyProduct();
        pp1.setId(p1.getId());
        pp1.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp3 = new PartyProduct();
        pp3.setId(p3.getId());
        pp3.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp4 = new PartyProduct();
        pp4.setId(p4.getId());
        pp4.setStatus(ProductStatus.PENDING);
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(pp1, pp3, pp4));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(LIMITED, "productRole2", p2.getId());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(LIMITED, "productRole3", p3.getId());
        ProductGrantedAuthority productGrantedAuthority4 = new ProductGrantedAuthority(LIMITED, "productRole4", p4.getId());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority2, productGrantedAuthority3, productGrantedAuthority4))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(2, products.size());
        HashMap<String, ProductStatus> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put(pp3.getId(), pp3.getStatus());
        expectedStatusMap.put(pp4.getId(), pp4.getStatus());
        products.forEach(product -> {
            Assertions.assertTrue(expectedStatusMap.containsKey(product.getId()));
            Assertions.assertEquals(expectedStatusMap.get(product.getId()), product.getStatus());
            Assertions.assertTrue(product.isAuthorized());
            Assertions.assertEquals(LIMITED.name(), product.getUserRole());
        });
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
        Product p4 = TestUtils.mockInstance(new Product(), 4);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(List.of(p1, p2, p3));
        PartyProduct pp1 = new PartyProduct();
        pp1.setId(p1.getId());
        pp1.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp3 = new PartyProduct();
        pp3.setId(p3.getId());
        pp3.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp4 = new PartyProduct();
        pp4.setId(p4.getId());
        pp4.setStatus(ProductStatus.PENDING);
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(pp1, pp3, pp4));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(ADMIN, "productRole2", p2.getId());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(ADMIN, "productRole3", p3.getId());
        SelfCareGrantedAuthority selfCareGrantedAuthority = new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority2, productGrantedAuthority3));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(selfCareGrantedAuthority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<Product> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(3, products.size());
        HashMap<String, ProductStatus> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put(pp1.getId(), pp1.getStatus());
        expectedStatusMap.put(p2.getId(), ProductStatus.INACTIVE);
        expectedStatusMap.put(pp3.getId(), pp3.getStatus());
        expectedStatusMap.put(pp4.getId(), pp4.getStatus());
        Set<String> expectedAuthorizedProducts = Set.of(p3.getId(), p2.getId());
        products.forEach(product -> {
            Assertions.assertTrue(expectedStatusMap.containsKey(product.getId()));
            Assertions.assertEquals(expectedStatusMap.get(product.getId()), product.getStatus());
            Assertions.assertEquals(expectedAuthorizedProducts.contains(product.getId()), product.isAuthorized());
            if (selfCareGrantedAuthority.getRoleOnProducts().containsKey(product.getId())) {
                Assertions.assertEquals(selfCareGrantedAuthority.getRoleOnProducts().get(product.getId()).getAuthority(), product.getUserRole());
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
    void getInstitutionProductUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_nullProductRole() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = null;
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional product role object is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_nullRole() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        Optional<SelfCareAuthority> role = null;
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional role object is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProductUsers() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(productId));
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        Optional<String> userId = Optional.empty();
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, userInfoFilter);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
        Mockito.verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        Optional<String> productId = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        Optional<SelfCareAuthority> role = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionUsers_nullProductIds() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = null;
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();

        // when
        Executable executable = () -> institutionService.getInstitutionUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional Product id object is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionUsers_nullProductRole() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.empty();
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = null;

        // when
        Executable executable = () -> institutionService.getInstitutionUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional product role object is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionUsers_nullRole() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.empty();
        Optional<SelfCareAuthority> role = null;
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Executable executable = () -> institutionService.getInstitutionUsers(institutionId, productId, role, productRole);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional role object is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionUser() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.empty();
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        Optional<String> userId = Optional.of("userId1");
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);

        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");

        userInfoMock1.setId("userId1");

        ProductInfo productInfo1 = new ProductInfo();

        String productId1 = "prod-1";
        productInfo1.setId(productId1);

        Map<String, ProductInfo> products1 = new HashMap<>();
        products1.put(productId1, productInfo1);
        userInfoMock1.setProducts(products1);

        Mockito.when(partyConnectorMock.getUsers(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1));
        Product product1 = TestUtils.mockInstance(new Product(), "setId");
        product1.setId(productId1);

        Map<String, Product> idToProductMap = Map.of(productId1, product1);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(new ArrayList<>(idToProductMap.values()));
        // when
        UserInfo userInfo = institutionService.getInstitutionUser(institutionId, userId.orElse(null));
        // then
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(1, userInfo.getProducts().size());
        for (String key : productInfoMap.keySet()) {
            ProductInfo productInfo = productInfoMap.get(key);
            Assertions.assertEquals(idToProductMap.get(productInfo.getId()).getTitle(), productInfo.getTitle());
        }

        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, userInfoFilter);
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProducts();
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void getInstitutionUser_nullUserId() {
        // given
        String institutionId = "institutionId";
        String userId = null;
        // when
        Executable executable = () -> institutionService.getInstitutionUser(institutionId, userId);
        // then

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A user id is required", e.getMessage());
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock);

    }

    @Test
    void getInstitutionUser_userNotFound() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.empty();
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        Optional<String> userId = Optional.of("userId1");
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        Mockito.when(partyConnectorMock.getUsers(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
        // when
        Executable executable = () -> institutionService.getInstitutionUser(institutionId, userId.get());
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        Assertions.assertEquals("No User found for the given userId", e.getMessage());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, userInfoFilter);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);

    }

    @Test
    void getInstitutionUsers() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.empty();
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        Optional<String> userId = Optional.empty();
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        UserInfo userInfoMock1 = TestUtils.mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = TestUtils.mockInstance(new UserInfo(), 2, "setId");

        ProductInfo productInfo1 = new ProductInfo();
        ProductInfo productInfo2 = new ProductInfo();
        ProductInfo productInfo3 = new ProductInfo();

        String productId1 = "prod-1";
        String productId2 = "prod-2";
        String productId3 = "prod-3";
        productInfo1.setId(productId1);
        productInfo2.setId(productId2);
        productInfo3.setId(productId3);
        Map<String, ProductInfo> products1 = new HashMap<>();
        products1.put(productId1, productInfo1);
        products1.put(productId2, productInfo2);
        userInfoMock1.setProducts(products1);
        HashMap<String, ProductInfo> products2 = new HashMap<>();
        products2.put(productId3, productInfo3);
        userInfoMock2.setProducts(products2);
        Mockito.when(partyConnectorMock.getUsers(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2));
        Product product1 = TestUtils.mockInstance(new Product(), 1, "setId");
        product1.setId(productId1);
        Product product2 = TestUtils.mockInstance(new Product(), 2, "setId");
        product2.setId(productId2);
        Product product3 = TestUtils.mockInstance(new Product(), 3, "setId");
        product3.setId(productId3);
        Map<String, Product> idToProductMap = Map.of(productId1, product1, productId2, product2, productId3, product3);
        Mockito.when(productsConnectorMock.getProducts())
                .thenReturn(new ArrayList<>(idToProductMap.values()));
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(2, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(2, userInfo.getProducts().size());
        for (String key : productInfoMap.keySet()) {
            ProductInfo productInfo = productInfoMap.get(key);
            Assertions.assertEquals(idToProductMap.get(productInfo.getId()).getTitle(), productInfo.getTitle());
        }

        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(institutionId, userInfoFilter);
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

    @ParameterizedTest
    @ValueSource(strings = {"MANAGER", "DELEGATE", "SUB_DELEGATE", "OPERATOR"})
    void createUsers(String partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRole = "productRole";
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setRole");
        CreateUserDto.Role roleMock = TestUtils.mockInstance(new CreateUserDto.Role(), "setProductRole");
        roleMock.setPartyRole(partyRole);
        roleMock.setProductRole(productRole);
        createUserDto.setRoles(Set.of(roleMock));
        ProductRoleInfo.ProductRole role = new ProductRoleInfo.ProductRole();
        role.setCode(productRole);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleInfo.setRoles(List.of(role));
        Mockito.when(productsConnectorMock.getProductRoleMappings(Mockito.anyString()))
                .thenReturn(Map.of(PartyRole.valueOf(partyRole), productRoleInfo));
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        if ("SUB_DELEGATE".equals(partyRole) || "OPERATOR".equals(partyRole)) {
            assertDoesNotThrow(executable);
            Mockito.verify(partyConnectorMock, Mockito.times(1))
                    .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), createUserDtoCaptor.capture());
            createUserDtoCaptor.getValue().getRoles().forEach(role1 -> {
                Assertions.assertEquals(partyRole, role1.getPartyRole());
            });
            TestUtils.reflectionEqualsByName(createUserDtoCaptor.getValue(), createUserDto);
            Mockito.verifyNoMoreInteractions(partyConnectorMock);
        } else {
            InvalidProductRoleException e = assertThrows(InvalidProductRoleException.class, executable);
            createUserDto.getRoles().forEach(role -> {
                Assertions.assertEquals(String.format("Product role '%s' is not valid", role.getProductRole()), e.getMessage());
            });
            Mockito.verifyNoInteractions(partyConnectorMock);
        }
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductRoleMappings(productId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
    }

}