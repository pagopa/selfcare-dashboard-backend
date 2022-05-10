package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.PartyConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.*;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.dashboard.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static org.junit.jupiter.api.Assertions.*;

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
    private PartyConnector partyConnectorMock;

    @MockBean
    private NotificationService notificationServiceMock;

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
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        Mockito.when(partyConnectorMock.getOnBoardedInstitution(Mockito.any()))
                .thenReturn(expectedInstitutionInfo);
        // when
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        // then
        assertSame(expectedInstitutionInfo, institutionInfo);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnBoardedInstitution(institutionId);
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutions() {
        // given
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        Mockito.when(partyConnectorMock.getOnBoardedInstitutions())
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getOnBoardedInstitutions();
        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_emptyProducts() {
        //given
        String institutionId = "institutionId";
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(Collections.emptyList());
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductsTree();
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_GrantedAuthorityOnDifferentInstId() {
        //given
        String institutionId = "institutionId";
        ProductTree product = TestUtils.mockInstance(new ProductTree());
        List<ProductTree> productList = List.of(product);
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(productList);
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(LIMITED, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority("institutionId2", Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertIterableEquals(productList, products);
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductsTree();
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
        Mockito.verifyNoInteractions(partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_limitedWithEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        ProductTree product = TestUtils.mockInstance(new ProductTree());
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(product));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(LIMITED, "productRole", "productId");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertTrue(products.isEmpty());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductsTree();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_adminWithEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        ProductTree product = TestUtils.mockInstance(new ProductTree());
        Product children = TestUtils.mockInstance(new Product());
        product.setChildren(List.of(children));
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(product));
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(Collections.emptyList());
        ProductGrantedAuthority productGrantedAuthority = new ProductGrantedAuthority(ADMIN, "productRole", product.getNode().getId());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, Collections.singleton(productGrantedAuthority))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertEquals(ProductStatus.INACTIVE, products.get(0).getNode().getStatus());
        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductsTree();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_limitedWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        ProductTree p1 = TestUtils.mockInstance(new ProductTree(), 1);
        ProductTree p2 = TestUtils.mockInstance(new ProductTree(), 2);
        ProductTree p3 = TestUtils.mockInstance(new ProductTree(), 3);
        ProductTree p4 = TestUtils.mockInstance(new ProductTree(), 4);
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(p1, p2, p3, p4));
        PartyProduct pp1 = new PartyProduct();
        pp1.setId(p1.getNode().getId());
        pp1.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp3 = new PartyProduct();
        pp3.setId(p3.getNode().getId());
        pp3.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp4 = new PartyProduct();
        pp4.setId(p4.getNode().getId());
        pp4.setStatus(ProductStatus.PENDING);
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(pp1, pp3, pp4));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(LIMITED, "productRole2", p2.getNode().getId());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(LIMITED, "productRole3", p3.getNode().getId());
        ProductGrantedAuthority productGrantedAuthority4 = new ProductGrantedAuthority(LIMITED, "productRole4", p4.getNode().getId());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority2, productGrantedAuthority3, productGrantedAuthority4))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(2, products.size());
        HashMap<String, ProductStatus> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put(pp3.getId(), pp3.getStatus());
        expectedStatusMap.put(pp4.getId(), pp4.getStatus());
        products.forEach(product -> {
            Assertions.assertTrue(expectedStatusMap.containsKey(product.getNode().getId()));
            Assertions.assertEquals(expectedStatusMap.get(product.getNode().getId()), product.getNode().getStatus());
            Assertions.assertTrue(product.getNode().isAuthorized());
            Assertions.assertEquals(LIMITED.name(), product.getNode().getUserRole());
        });

        Mockito.verify(productsConnectorMock, Mockito.times(1)).getProductsTree();
        Mockito.verify(partyConnectorMock, Mockito.times(1)).getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_adminWithNotEmptyInstProducts() {
        //given
        String institutionId = "institutionId";
        ProductTree p1 = TestUtils.mockInstance(new ProductTree(), 1);
        ProductTree p2 = TestUtils.mockInstance(new ProductTree(), 2);
        ProductTree p3 = TestUtils.mockInstance(new ProductTree(), 3);
        ProductTree p4 = TestUtils.mockInstance(new ProductTree(), 4);
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(p1, p2, p3));
        PartyProduct pp1 = new PartyProduct();
        pp1.setId(p1.getNode().getId());
        pp1.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp3 = new PartyProduct();
        pp3.setId(p3.getNode().getId());
        pp3.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp4 = new PartyProduct();
        pp4.setId(p4.getNode().getId());
        pp4.setStatus(ProductStatus.PENDING);
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(pp1, pp3, pp4));
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(ADMIN, "productRole2", p2.getNode().getId());
        ProductGrantedAuthority productGrantedAuthority3 = new ProductGrantedAuthority(ADMIN, "productRole3", p3.getNode().getId());
        SelfCareGrantedAuthority selfCareGrantedAuthority = new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority2, productGrantedAuthority3));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(selfCareGrantedAuthority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(3, products.size());
        HashMap<String, ProductStatus> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put(pp1.getId(), pp1.getStatus());
        expectedStatusMap.put(p2.getNode().getId(), ProductStatus.INACTIVE);
        expectedStatusMap.put(pp3.getId(), pp3.getStatus());
        expectedStatusMap.put(pp4.getId(), pp4.getStatus());
        Set<String> expectedAuthorizedProducts = Set.of(p3.getNode().getId(), p2.getNode().getId());
        products.forEach(product -> {
            Assertions.assertTrue(expectedStatusMap.containsKey(product.getNode().getId()));
            Assertions.assertEquals(expectedStatusMap.get(product.getNode().getId()), product.getNode().getStatus());
            Assertions.assertEquals(expectedAuthorizedProducts.contains(product.getNode().getId()), product.getNode().isAuthorized());
            if (selfCareGrantedAuthority.getRoleOnProducts().containsKey(product.getNode().getId())) {
                Assertions.assertEquals(selfCareGrantedAuthority.getRoleOnProducts().get(product.getNode().getId()).getAuthority(), product.getNode().getUserRole());
            } else {
                Assertions.assertNull(product.getNode().getUserRole());
            }
        });
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getInstitutionProducts(institutionId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock, partyConnectorMock);
    }

    @Test
    void getInstitutionProducts_limitedNotEmptyInstitutionWithChildren() {
        //given
        String institutionId = "institutionId";
        ProductTree p1 = TestUtils.mockInstance(new ProductTree(), 1);
        ProductTree p2 = TestUtils.mockInstance(new ProductTree(), 2);
        Product children1 = TestUtils.mockInstance(new Product(), 1);
        Product children2 = TestUtils.mockInstance(new Product(), 2);
        p1.setChildren(List.of(children1, children2));
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(p1, p2));
        PartyProduct pp1 = new PartyProduct();
        pp1.setId(p1.getNode().getId());
        pp1.setStatus(ProductStatus.ACTIVE);
        PartyProduct pp2 = new PartyProduct();
        pp2.setId(p2.getNode().getId());
        pp2.setStatus(ProductStatus.ACTIVE);
        Mockito.when(partyConnectorMock.getInstitutionProducts(Mockito.any()))
                .thenReturn(List.of(pp1, pp2));
        ProductGrantedAuthority productGrantedAuthority1 = new ProductGrantedAuthority(LIMITED, "productRole1", p1.getNode().getId());
        ProductGrantedAuthority productGrantedAuthority2 = new ProductGrantedAuthority(LIMITED, "productRole2", p2.getNode().getId());
        SelfCareGrantedAuthority selfCareGrantedAuthority = new SelfCareGrantedAuthority(institutionId, List.of(productGrantedAuthority1, productGrantedAuthority2));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(null,
                null,
                Collections.singletonList(selfCareGrantedAuthority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when
        List<ProductTree> products = institutionService.getInstitutionProducts(institutionId);
        //then
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(2, products.size());
        HashMap<String, ProductStatus> expectedStatusMap = new HashMap<>();
        expectedStatusMap.put(pp1.getId(), pp1.getStatus());
        expectedStatusMap.put(p2.getNode().getId(), ProductStatus.ACTIVE);
        expectedStatusMap.put(pp2.getId(), pp2.getStatus());
        expectedStatusMap.put(p1.getNode().getId(), ProductStatus.ACTIVE);
        Set<String> expectedAuthorizedProducts = Set.of(p1.getNode().getId(), p2.getNode().getId());
        products.forEach(product -> {
            Assertions.assertTrue(expectedStatusMap.containsKey(product.getNode().getId()));
            Assertions.assertEquals(expectedStatusMap.get(product.getNode().getId()), product.getNode().getStatus());
            Assertions.assertEquals(expectedAuthorizedProducts.contains(product.getNode().getId()), product.getNode().isAuthorized());
            if (selfCareGrantedAuthority.getRoleOnProducts().containsKey(product.getNode().getId())) {
                Assertions.assertEquals(selfCareGrantedAuthority.getRoleOnProducts().get(product.getNode().getId()).getAuthority(), product.getNode().getUserRole());
            } else {
                Assertions.assertNull(product.getNode().getUserRole());
            }
        });
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
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
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)));
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(role, capturedFilter.getRole());
        assertEquals(productId, capturedFilter.getProductId().get());
        assertEquals(productRole, capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());

        Mockito.verifyNoMoreInteractions(partyConnectorMock);
        Mockito.verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_nullAllowedStates() {
        //given
        InstitutionServiceImpl institutionService = new InstitutionServiceImpl(null, partyConnectorMock, productsConnectorMock, notificationServiceMock);
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(productId));
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        //when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(role, capturedFilter.getRole());
        assertEquals(productId, capturedFilter.getProductId().get());
        assertEquals(productRole, capturedFilter.getProductRoles());
        assertEquals(Optional.empty(), capturedFilter.getUserId());
        assertEquals(Optional.empty(), capturedFilter.getAllowedStates());

        Mockito.verifyNoMoreInteractions(partyConnectorMock);
    }

    @Test
    void emptyAllowedStates() {
        //given
        InstitutionServiceImpl institutionService = new InstitutionServiceImpl(new String[0], partyConnectorMock, productsConnectorMock, notificationServiceMock);
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of(productId));
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        //when
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
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
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
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(idToProductMap.values().stream()
                        .map(product -> {
                            final ProductTree productTree = new ProductTree();
                            productTree.setNode(product);
                            return productTree;
                        }).collect(Collectors.toList()));
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
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(Optional.empty(), capturedFilter.getProductId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
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
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(Optional.empty(), capturedFilter.getProductId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }


    @Test
    void getInstitutionUser_noMatchingProduct() {
        // given
        String institutionId = "institutionId";
        Optional<String> userId = Optional.of("userId1");
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setProducts");
        final ProductInfo productInfoMock = TestUtils.mockInstance(new ProductInfo(), 1, "setRoleInfos");
        userInfoMock.setProducts(Map.of(productInfoMock.getId(), productInfoMock));
        Mockito.when(partyConnectorMock.getUsers(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock));
        final ProductTree productTree = TestUtils.mockInstance(new ProductTree(), 2, "setChildren");
        productTree.setChildren(Collections.emptyList());
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(productTree));
        // when
        Executable executable = () -> institutionService.getInstitutionUser(institutionId, userId.get());
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("No matching product found with id " + productInfoMock.getId(), e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(Optional.empty(), capturedFilter.getRole());
        assertEquals(Optional.empty(), capturedFilter.getProductId());
        assertEquals(Optional.empty(), capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }


    @Test
    void getInstitutionUser_matchingChildProduct() {
        // given
        String institutionId = "institutionId";
        Optional<String> userId = Optional.of("userId1");
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        UserInfo userInfoMock = TestUtils.mockInstance(new UserInfo(), "setProducts");
        final ProductInfo productInfoMock1 = TestUtils.mockInstance(new ProductInfo(), 1, "setRoleInfos");
        final ProductInfo productInfoMock2 = TestUtils.mockInstance(new ProductInfo(), 2, "setRoleInfos");
        final Map<String, ProductInfo> productInfoMapMock = new HashMap<>();
        productInfoMapMock.put(productInfoMock1.getId(), productInfoMock1);
        productInfoMapMock.put(productInfoMock2.getId(), productInfoMock2);
        userInfoMock.setProducts(productInfoMapMock);
        Mockito.when(partyConnectorMock.getUsers(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(userInfoMock));
        final ProductTree productTreeMock = TestUtils.mockInstance(new ProductTree(), 1, "setChildren");
        final Product productChildMock = TestUtils.mockInstance(new Product(), 2);
        productTreeMock.setChildren(List.of(productChildMock));
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(productTreeMock));
        // when
        UserInfo userInfo = institutionService.getInstitutionUser(institutionId, userId.get());
        // then
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(1, userInfo.getProducts().size());
        Assertions.assertFalse(userInfo.getProducts().containsKey(productInfoMock2.getId()));
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), Mockito.any(UserInfo.UserInfoFilter.class));
        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
        Mockito.verifyNoMoreInteractions(partyConnectorMock, productsConnectorMock);
    }

    @Test
    void getInstitutionUsers() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.of("productId");
        Optional<SelfCareAuthority> role = Optional.of(ADMIN);
        Optional<Set<String>> productRole = Optional.of(Set.of("Operatore"));
        Optional<String> userId = Optional.empty();

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
        Mockito.when(productsConnectorMock.getProductsTree())
                .thenReturn(idToProductMap.values().stream()
                        .map(product -> {
                            final ProductTree productTree = new ProductTree();
                            productTree.setNode(product);
                            return productTree;
                        }).collect(Collectors.toList()));
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

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        Mockito.verify(partyConnectorMock, Mockito.times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(role, capturedFilter.getRole());
        assertEquals(productId, capturedFilter.getProductId());
        assertEquals(productRole, capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(Optional.of(EnumSet.of(RelationshipState.ACTIVE, RelationshipState.SUSPENDED)), capturedFilter.getAllowedStates());

        Mockito.verify(productsConnectorMock, Mockito.times(1))
                .getProductsTree();
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
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock, notificationServiceMock);
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
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock, notificationServiceMock);
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
        Mockito.verifyNoInteractions(productsConnectorMock, partyConnectorMock, notificationServiceMock);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void createUsers(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoleCode = "productRoleCode";
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setRole");
        CreateUserDto.Role roleMock = TestUtils.mockInstance(new CreateUserDto.Role(), "setProductRole");
        roleMock.setPartyRole(partyRole);
        roleMock.setProductRole(productRoleCode);
        createUserDto.setRoles(Set.of(roleMock));
        Product product = TestUtils.mockInstance(new Product());
        ProductRoleInfo.ProductRole productRole = new ProductRoleInfo.ProductRole();
        product.setId(productId);
        productRole.setCode(productRoleCode);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleInfo.setRoles(List.of(productRole));
        EnumMap<PartyRole, ProductRoleInfo> map = new EnumMap<>(PartyRole.class);
        map.put(partyRole, productRoleInfo);
        product.setRoleMappings(map);
        Mockito.when(productsConnectorMock.getProduct(Mockito.anyString()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        if (PartyRole.SUB_DELEGATE.equals(partyRole) || PartyRole.OPERATOR.equals(partyRole)) {
            assertDoesNotThrow(executable);
            Mockito.verify(partyConnectorMock, Mockito.times(1))
                    .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), createUserDtoCaptor.capture());
            Mockito.verify(notificationServiceMock, Mockito.times(1)).
                    sendCreatedUserNotification(institutionId, product.getTitle(), createUserDto.getEmail());
            createUserDtoCaptor.getValue().getRoles().forEach(role1 -> Assertions.assertEquals(partyRole, role1.getPartyRole()));
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
                .getProduct(productId);
        Mockito.verifyNoMoreInteractions(productsConnectorMock);
    }

}