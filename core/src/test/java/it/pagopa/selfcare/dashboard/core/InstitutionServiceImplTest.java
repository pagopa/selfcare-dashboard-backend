package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.OnboardedProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.*;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.model.user.User.Fields;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.core.config.CoreTestConfig;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
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

import static it.pagopa.selfcare.commons.base.security.PartyRole.MANAGER;
import static it.pagopa.selfcare.commons.base.security.PartyRole.OPERATOR;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.SUSPENDED;
import static it.pagopa.selfcare.dashboard.connector.model.user.User.Fields.*;
import static it.pagopa.selfcare.dashboard.core.InstitutionServiceImpl.REQUIRED_GEOGRAPHIC_TAXONOMIES;
import static it.pagopa.selfcare.dashboard.core.InstitutionServiceImpl.REQUIRED_TOKEN_ID_MESSAGE;
import static it.pagopa.selfcare.dashboard.core.PnPGInstitutionServiceImpl.REQUIRED_INSTITUTION_MESSAGE;
import static it.pagopa.selfcare.dashboard.core.PnPGInstitutionServiceImpl.REQUIRED_UPDATE_RESOURCE_MESSAGE;
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
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        when(msCoreConnectorMock.getOnBoardedInstitution(any()))
                .thenReturn(expectedInstitutionInfo);
        // when
        InstitutionInfo institutionInfo = institutionService.getInstitution(institutionId);
        // then
        assertSame(expectedInstitutionInfo, institutionInfo);
        verify(msCoreConnectorMock, times(1))
                .getOnBoardedInstitution(institutionId);
        verifyNoMoreInteractions(msCoreConnectorMock);
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
    void getInstitutions() {
        // given
        String userId = "userId";
        InstitutionInfo expectedInstitutionInfo = new InstitutionInfo();
        when(msCoreConnectorMock.getUserProducts(userId))
                .thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionInfo> institutions = institutionService.getInstitutions(userId);
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verify(msCoreConnectorMock, times(1))
                .getUserProducts(userId);
        verifyNoMoreInteractions(msCoreConnectorMock);
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
        ProductTree product = mockInstance(new ProductTree());
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
    }

    @Test
    void getInstitutionProductUsers() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setAllowedStates(List.of(ACTIVE, SUSPENDED));
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        UserInfo userInfo = mockInstance(new UserInfo());
        String userId = UUID.randomUUID().toString();
        userInfo.setId(userId);
        User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        user.setWorkContacts(workContact);
        when(msCoreConnectorMock.getUsers(anyString(), any()))
                .thenReturn(Collections.singletonList(userInfo));
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        Assertions.assertNull(capturedFilter.getRole());
        assertEquals(productId, capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertNull(capturedFilter.getUserId());
        ArgumentCaptor<EnumSet<Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(Mockito.eq(userId), filedsCaptor.capture());
        EnumSet<Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        verifyNoMoreInteractions(msCoreConnectorMock, userRegistryConnector);
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void getInstitutionProductUsers_nullAllowedStates() {
        //given
        InstitutionServiceImpl institutionService = new InstitutionServiceImpl(null, userRegistryConnector, productsConnectorMock, msCoreConnectorMock);
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        UserInfo userInfo = mockInstance(new UserInfo());
        String userId = UUID.randomUUID().toString();
        userInfo.setId(userId);
        User user = mockInstance(new User());
        user.setId(userId);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        user.setWorkContacts(workContact);
        when(msCoreConnectorMock.getUsers(anyString(), any()))
                .thenReturn(Collections.singletonList(userInfo));
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenReturn(user);
        //when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        userInfos.forEach(userInfo1 -> {
            TestUtils.checkNotNullFields(userInfo1, "products");
            TestUtils.checkNotNullFields(userInfo1.getUser());
        });
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertEquals(productId, capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertNull( capturedFilter.getUserId());
        assertNull(capturedFilter.getAllowedStates());
        ArgumentCaptor<EnumSet<Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(Mockito.eq(userId), filedsCaptor.capture());
        EnumSet<Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        assertFalse(capturedFields.contains(fiscalCode));
        verifyNoMoreInteractions(msCoreConnectorMock, userRegistryConnector);
    }

    @Test
    void emptyAllowedStates() {
        //given
        InstitutionServiceImpl institutionService = new InstitutionServiceImpl(new String[0], userRegistryConnector, productsConnectorMock, msCoreConnectorMock);
        String institutionId = "institutionId";
        String productId = "productId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<Set<String>> productRole = Optional.empty();
        //when
        Collection<UserInfo> userInfos = institutionService.getInstitutionProductUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        verify(msCoreConnectorMock, times(1))
                .getUsers(institutionId, userInfoFilter);
        verifyNoMoreInteractions(msCoreConnectorMock);
        verifyNoInteractions(productsConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);
    }

    @Test
    void getInstitutionUser() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId("userId1");

        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        userInfoMock1.setId("userId1");

        ProductInfo productInfo1 = new ProductInfo();
        String productId1 = "prod-1";
        productInfo1.setId(productId1);

        Map<String, ProductInfo> products1 = new HashMap<>();
        products1.put(productId1, productInfo1);
        userInfoMock1.setProducts(products1);

        User userMock = mockInstance(new User());
        userMock.setId("userId1");
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        userMock.setWorkContacts(workContact);
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenReturn(userMock);

        when(msCoreConnectorMock.getUsers(any(), any()))
                .thenReturn(List.of(userInfoMock1));
        Product product1 = mockInstance(new Product(), "setId");
        product1.setId(productId1);

        Map<String, Product> idToProductMap = Map.of(productId1, product1);
        when(productsConnectorMock.getProductsTree())
                .thenReturn(idToProductMap.values().stream()
                        .map(product -> {
                            final ProductTree productTree = new ProductTree();
                            productTree.setNode(product);
                            return productTree;
                        }).collect(Collectors.toList()));
        // when
        UserInfo userInfo = institutionService.getInstitutionUser(institutionId, userInfoFilter.getUserId());
        // then
        TestUtils.checkNotNullFields(userInfo);
        TestUtils.checkNotNullFields(userInfo.getUser());
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(1, userInfo.getProducts().size());
        for (String key : productInfoMap.keySet()) {
            ProductInfo productInfo = productInfoMap.get(key);
            Assertions.assertEquals(idToProductMap.get(productInfo.getId()).getTitle(), productInfo.getTitle());
        }
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertNull(capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertEquals(userInfoFilter.getUserId(), capturedFilter.getUserId());
        verify(productsConnectorMock, times(1))
                .getProductsTree();
        ArgumentCaptor<EnumSet<Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(Mockito.eq("userId1"), filedsCaptor.capture());
        EnumSet<Fields> capturedFields = filedsCaptor.getValue();
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        assertTrue(capturedFields.contains(fiscalCode));
        verifyNoMoreInteractions(msCoreConnectorMock, productsConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock);

    }

    @Test
    void getInstitutionUser_userNotFound() {
        // given
        String institutionId = "institutionId";
        String userId = "userId1";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        when(msCoreConnectorMock.getUsers(any(), any()))
                .thenReturn(Collections.emptyList());
        // when
        Executable executable = () -> institutionService.getInstitutionUser(institutionId, userId);
        // then
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);
        Assertions.assertEquals("No User found for the given userId", e.getMessage());
        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertNull(capturedFilter.getRole());
        assertNull(capturedFilter.getProductId());
        assertNull(capturedFilter.getProductRoles());
        assertEquals(userId, capturedFilter.getUserId());
        assertEquals(List.of(ACTIVE, SUSPENDED), capturedFilter.getAllowedStates());
        verify(productsConnectorMock, times(1))
                .getProductsTree();
        verifyNoMoreInteractions(msCoreConnectorMock, productsConnectorMock, userRegistryConnector);
    }


    @Test
    void getInstitutionUser_noMatchingProduct() {
        // given
        String institutionId = "institutionId";
        String userId = "userId1";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setProducts");
        final ProductInfo productInfoMock = mockInstance(new ProductInfo(), 1, "setRoleInfos");
        Map<String, ProductInfo> map = new HashMap<>();
        map.put(productInfoMock.getId(), productInfoMock);
        userInfoMock.setProducts(map);
        when(msCoreConnectorMock.getUsers(any(), any()))
                .thenReturn(List.of(userInfoMock));
        final ProductTree productTree = mockInstance(new ProductTree(), 2, "setChildren");
        productTree.setChildren(Collections.emptyList());
        when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(productTree));
        User user = new User();
        user.setId("id");
        CertifiedField<String> nameCert = new CertifiedField<>();
        nameCert.setValue("name");
        user.setName(nameCert);
        CertifiedField<String> surnameCert = new CertifiedField<>();
        surnameCert.setValue("surname");
        user.setFamilyName(surnameCert);
        user.setFiscalCode("fiscalCode");
        CertifiedField<String> mailCert = new CertifiedField<>();
        mailCert.setValue("surname");
        user.setEmail(mailCert);
        user.setWorkContacts(new HashMap<>());
        when(userRegistryConnector.getUserByInternalId(any(), any())).thenReturn(user);
        // when
        UserInfo userInfo = institutionService.getInstitutionUser(institutionId, userId);

        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(0, userInfo.getProducts().size());
        TestUtils.checkNotNullFields(userInfo);
        TestUtils.checkNotNullFields(userInfo.getUser());
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), any(UserInfo.UserInfoFilter.class));
        verify(productsConnectorMock, times(1))
                .getProductsTree();
        ArgumentCaptor<EnumSet<Fields>> fieldsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(any(), fieldsCaptor.capture());
        EnumSet<Fields> capturedFields = fieldsCaptor.getValue();
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        assertTrue(capturedFields.contains(fiscalCode));
        verifyNoMoreInteractions(msCoreConnectorMock, productsConnectorMock);
    }


    @Test
    void getInstitutionUser_matchingChildProduct() {
        // given
        String institutionId = "institutionId";
        String userId = "userId1";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setUserId(userId);
        UserInfo userInfoMock = mockInstance(new UserInfo(), "setProducts");
        final ProductInfo productInfoMock1 = mockInstance(new ProductInfo(), 1, "setRoleInfos");
        final ProductInfo productInfoMock2 = mockInstance(new ProductInfo(), 2, "setRoleInfos");
        final Map<String, ProductInfo> productInfoMapMock = new HashMap<>();
        productInfoMapMock.put(productInfoMock1.getId(), productInfoMock1);
        productInfoMapMock.put(productInfoMock2.getId(), productInfoMock2);
        userInfoMock.setProducts(productInfoMapMock);
        userInfoMock.setId("userId1");
        User userMock = mockInstance(new User());
        userMock.setId("userId1");
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        userMock.setWorkContacts(workContact);
        when(userRegistryConnector.getUserByInternalId(anyString(), any()))
                .thenReturn(userMock);

        when(msCoreConnectorMock.getUsers(any(), any()))
                .thenReturn(List.of(userInfoMock));
        final ProductTree productTreeMock = mockInstance(new ProductTree(), 1, "setChildren");
        final Product productChildMock = mockInstance(new Product(), 2);
        productTreeMock.setChildren(List.of(productChildMock));
        when(productsConnectorMock.getProductsTree())
                .thenReturn(List.of(productTreeMock));
        // when
        UserInfo userInfo = institutionService.getInstitutionUser(institutionId, userId);
        // then
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(1, userInfo.getProducts().size());
        TestUtils.checkNotNullFields(userInfo);
        TestUtils.checkNotNullFields(userInfo.getUser());
        Assertions.assertFalse(userInfo.getProducts().containsKey(productInfoMock2.getId()));
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), any(UserInfo.UserInfoFilter.class));
        verify(productsConnectorMock, times(1))
                .getProductsTree();
        ArgumentCaptor<EnumSet<Fields>> fieldsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(1))
                .getUserByInternalId(Mockito.eq("userId1"), fieldsCaptor.capture());
        EnumSet<Fields> capturedFields = fieldsCaptor.getValue();
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        assertTrue(capturedFields.contains(fiscalCode));
        verifyNoMoreInteractions(msCoreConnectorMock, productsConnectorMock);
    }

    @Test
    void getInstitutionUsers() {
        // given
        String institutionId = "institutionId";
        Optional<String> productId = Optional.of("productId");
        Optional<SelfCareAuthority> role = Optional.of(ADMIN);
        Optional<Set<String>> productRole = Optional.of(Set.of("Operatore"));
        Optional<String> userId = Optional.empty();

        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        UserInfo userInfoMock1 = mockInstance(new UserInfo(), 1, "setId");
        UserInfo userInfoMock2 = mockInstance(new UserInfo(), 2, "setId");
        userInfoMock1.setId(userId1);
        userInfoMock2.setId(userId2);
        User userMock1 = mockInstance(new User(), 1, "setId");
        User userMock2 = mockInstance(new User(), 2, "setId");
        userMock1.setId(userId1);
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(institutionId, contact);
        userMock1.setWorkContacts(workContact);
        userMock2.setId(userId2);
        userMock2.setWorkContacts(workContact);
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
        when(msCoreConnectorMock.getUsers(any(), any()))
                .thenReturn(List.of(userInfoMock1, userInfoMock2));
        Product product1 = mockInstance(new Product(), 1, "setId");
        product1.setId(productId1);
        Product product2 = mockInstance(new Product(), 2, "setId");
        product2.setId(productId2);
        Product product3 = mockInstance(new Product(), 3, "setId");
        product3.setId(productId3);
        Map<String, Product> idToProductMap = Map.of(productId1, product1, productId2, product2, productId3, product3);
        when(productsConnectorMock.getProductsTree())
                .thenReturn(idToProductMap.values().stream()
                        .map(product -> {
                            final ProductTree productTree = new ProductTree();
                            productTree.setNode(product);
                            return productTree;
                        }).collect(Collectors.toList()));
        when(userRegistryConnector.getUserByInternalId(Mockito.eq(userId1), any()))
                .thenReturn(userMock1);
        when(userRegistryConnector.getUserByInternalId(Mockito.eq(userId2), any()))
                .thenReturn(userMock2);
        // when
        Collection<UserInfo> userInfos = institutionService.getInstitutionUsers(institutionId, productId, role, productRole);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(2, userInfos.size());
        userInfos.forEach(userInfo -> {
            TestUtils.checkNotNullFields(userInfo);
            TestUtils.checkNotNullFields(userInfo.getUser());
        });
        UserInfo userInfo = userInfos.iterator().next();
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertNotNull(userInfo.getProducts());
        Assertions.assertEquals(2, userInfo.getProducts().size());
        for (String key : productInfoMap.keySet()) {
            ProductInfo productInfo = productInfoMap.get(key);
            Assertions.assertEquals(idToProductMap.get(productInfo.getId()).getTitle(), productInfo.getTitle());
        }

        ArgumentCaptor<UserInfo.UserInfoFilter> filterCaptor = ArgumentCaptor.forClass(UserInfo.UserInfoFilter.class);
        verify(msCoreConnectorMock, times(1))
                .getUsers(Mockito.eq(institutionId), filterCaptor.capture());
        UserInfo.UserInfoFilter capturedFilter = filterCaptor.getValue();
        assertEquals(role.get(), capturedFilter.getRole());
        assertEquals(productId.get(), capturedFilter.getProductId());
        assertNull(capturedFilter.getUserId());
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EnumSet<Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(2))
                .getUserByInternalId(userIdCaptor.capture(), filedsCaptor.capture());
        EnumSet<Fields> capturedFields = filedsCaptor.getValue();
        List<String> userIds = userIdCaptor.getAllValues();
        assertEquals(userIds, List.of(userId1, userId2));
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        assertFalse(capturedFields.contains(fiscalCode));
        verify(productsConnectorMock, times(1))
                .getProductsTree();
        verifyNoMoreInteractions(msCoreConnectorMock, productsConnectorMock);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock, userRegistryConnector);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock, userRegistryConnector);
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
        verifyNoInteractions(productsConnectorMock, msCoreConnectorMock, userRegistryConnector);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void createUsers(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoleCode1 = "productRoleCode";
        String productRoleLabel1 = "productRoleLabel";
        String productRoleCode2 = "productRoleCode2";
        String productRoleLabel2 = "productRoleLabel2";
        String productRoleCode3 = "productRoleCode3";
        String productRoleLabel3 = "productRoleLabel3";
        UUID id = UUID.randomUUID();
        UserId userId = new UserId();
        userId.setId(id);
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRole");
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        CreateUserDto.Role roleMock2 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        CreateUserDto.Role roleMock3 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        if (PartyRole.MANAGER.equals(partyRole) || PartyRole.DELEGATE.equals(partyRole)) {
            roleMock1.setPartyRole(partyRole);
            roleMock1.setProductRole(productRoleCode1);
            createUserDto.setRoles(Set.of(roleMock1));
        } else {
            roleMock1.setPartyRole(partyRole);
            roleMock1.setProductRole(productRoleCode1);
            roleMock2.setPartyRole(partyRole);
            roleMock2.setProductRole(productRoleCode2);
            roleMock3.setPartyRole(partyRole);
            roleMock3.setProductRole(productRoleCode3);
            createUserDto.setRoles(Set.of(roleMock1, roleMock2, roleMock3));
        }
        Product product = mockInstance(new Product());
        product.setId(productId);
        ProductRoleInfo.ProductRole productRole1 = new ProductRoleInfo.ProductRole();
        productRole1.setCode(productRoleCode1);
        productRole1.setLabel(productRoleLabel1);
        ProductRoleInfo.ProductRole productRole2 = new ProductRoleInfo.ProductRole();
        productRole2.setCode(productRoleCode2);
        productRole2.setLabel(productRoleLabel2);
        ProductRoleInfo.ProductRole productRole3 = new ProductRoleInfo.ProductRole();
        productRole3.setCode(productRoleCode3);
        productRole3.setLabel(productRoleLabel3);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleInfo.setRoles(List.of(productRole1, productRole2, productRole3));
        EnumMap<PartyRole, ProductRoleInfo> map = new EnumMap<>(PartyRole.class);
        map.put(partyRole, productRoleInfo);
        product.setRoleMappings(map);
        when(userRegistryConnector.saveUser(any()))
                .thenReturn(userId);
        when(productsConnectorMock.getProduct(anyString()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.createUsers(institutionId, productId, createUserDto);
        // then
        if (PartyRole.SUB_DELEGATE.equals(partyRole) || OPERATOR.equals(partyRole)) {
            assertDoesNotThrow(executable);
            verify(userRegistryConnector, times(1))
                    .saveUser(createUserDto.getUser());
            verify(msCoreConnectorMock, times(1))
                    .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), Mockito.eq(id.toString()), createUserDtoCaptor.capture(), eq("setTitle"));
            createUserDtoCaptor.getValue().getRoles().forEach(role1 -> {
                createUserDto.getRoles().forEach(role -> {
                    if (role.getLabel().equals(role1.getLabel())) {
                        Assertions.assertEquals(role.getPartyRole(), role1.getPartyRole());
                        Assertions.assertEquals(role.getLabel(), role1.getLabel());
                    }
                });
                {
                }
            });
            TestUtils.reflectionEqualsByName(createUserDtoCaptor.getValue(), createUserDto);
        } else {
            InvalidProductRoleException e = assertThrows(InvalidProductRoleException.class, executable);
            createUserDto.getRoles().forEach(role -> {
                Assertions.assertEquals(String.format("Product role '%s' is not valid", role.getProductRole()), e.getMessage());
            });
            verifyNoInteractions(msCoreConnectorMock);
        }
        verify(productsConnectorMock, times(1))
                .getProduct(productId);
        verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void createUsers() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoleCode1 = "productRoleCode";
        String productRoleLabel1 = "productRoleLabel";
        UUID id = UUID.randomUUID();
        UserId userId = new UserId();
        userId.setId(id);
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRole");
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        roleMock1.setPartyRole(OPERATOR);
        roleMock1.setProductRole(productRoleCode1);
        createUserDto.setRoles(Set.of(roleMock1));
        Product product = mockInstance(new Product());
        product.setId(productId);
        ProductRoleInfo.ProductRole productRole1 = new ProductRoleInfo.ProductRole();
        productRole1.setCode(productRoleCode1);
        productRole1.setLabel(productRoleLabel1);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleInfo.setRoles(List.of(productRole1));
        EnumMap<PartyRole, ProductRoleInfo> map = new EnumMap<>(PartyRole.class);
        map.put(OPERATOR, productRoleInfo);
        product.setRoleMappings(map);
        when(userRegistryConnector.saveUser(any()))
                .thenReturn(userId);
        when(productsConnectorMock.getProduct(anyString()))
                .thenReturn(product);
        //when
        UserId result = institutionService.createUsers(institutionId, productId, createUserDto);
        //then
        assertEquals(userId.getId(), result.getId());
        verify(userRegistryConnector, times(1))
                .saveUser(createUserDto.getUser());
        verify(msCoreConnectorMock, times(1))
                .checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId.getId().toString());
        verify(msCoreConnectorMock, times(1))
                .createUsers(institutionId, productId, userId.getId().toString(), createUserDto, "setTitle");
        verify(productsConnectorMock, times(1))
                .getProduct(productId);
        verifyNoMoreInteractions(userRegistryConnector, msCoreConnectorMock);
    }

    @Test
    void addUserProductRoles_nullInstitutionId() {
        //given
        String institutionId = null;
        String productId = null;
        String userId = null;
        CreateUserDto createUserDto = null;
        //when
        Executable executable = () -> institutionService.addUserProductRoles(institutionId, productId, userId, createUserDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void addUserProductRoles_nullProductId() {
        //given
        String institutionId = "institutionId";
        String productId = null;
        String userId = null;
        CreateUserDto createUserDto = null;
        //when
        Executable executable = () -> institutionService.addUserProductRoles(institutionId, productId, userId, createUserDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void addUserProductRoles_nullUserId() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = null;
        CreateUserDto createUserDto = null;
        //when
        Executable executable = () -> institutionService.addUserProductRoles(institutionId, productId, userId, createUserDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User id is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock);
    }

    @Test
    void addUserProductRoles_nullUser() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        CreateUserDto createUserDto = null;
        //when
        Executable executable = () -> institutionService.addUserProductRoles(institutionId, productId, userId, createUserDto);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An User is required", e.getMessage());
        verifyNoInteractions(productsConnectorMock);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void addUserProductRoles(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoleCode1 = "productRoleCode";
        String productRoleLabel1 = "productRoleLabel";
        String productRoleCode2 = "productRoleCode2";
        String productRoleLabel2 = "productRoleLabel2";
        String productRoleCode3 = "productRoleCode3";
        String productRoleLabel3 = "productRoleLabel3";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRole");
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        CreateUserDto.Role roleMock2 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        CreateUserDto.Role roleMock3 = mockInstance(new CreateUserDto.Role(), "setProductRole");
        if (PartyRole.MANAGER.equals(partyRole) || PartyRole.DELEGATE.equals(partyRole)) {
            roleMock1.setPartyRole(partyRole);
            roleMock1.setProductRole(productRoleCode1);
            createUserDto.setRoles(Set.of(roleMock1));
        } else {
            roleMock1.setPartyRole(partyRole);
            roleMock1.setProductRole(productRoleCode1);
            roleMock2.setPartyRole(partyRole);
            roleMock2.setProductRole(productRoleCode2);
            roleMock3.setPartyRole(partyRole);
            roleMock3.setProductRole(productRoleCode3);
            createUserDto.setRoles(Set.of(roleMock1, roleMock2, roleMock3));
        }
        Product product = mockInstance(new Product());
        product.setId(productId);
        ProductRoleInfo.ProductRole productRole1 = new ProductRoleInfo.ProductRole();
        productRole1.setCode(productRoleCode1);
        productRole1.setLabel(productRoleLabel1);
        ProductRoleInfo.ProductRole productRole2 = new ProductRoleInfo.ProductRole();
        productRole2.setCode(productRoleCode2);
        productRole2.setLabel(productRoleLabel2);
        ProductRoleInfo.ProductRole productRole3 = new ProductRoleInfo.ProductRole();
        productRole3.setCode(productRoleCode3);
        productRole3.setLabel(productRoleLabel3);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        productRoleInfo.setRoles(List.of(productRole1, productRole2, productRole3));
        EnumMap<PartyRole, ProductRoleInfo> map = new EnumMap<>(PartyRole.class);
        map.put(partyRole, productRoleInfo);
        product.setRoleMappings(map);
        when(productsConnectorMock.getProduct(anyString()))
                .thenReturn(product);
        // when
        Executable executable = () -> institutionService.addUserProductRoles(institutionId, productId, userId, createUserDto);
        // then
        if (PartyRole.SUB_DELEGATE.equals(partyRole) || OPERATOR.equals(partyRole)) {
            assertDoesNotThrow(executable);
            verify(msCoreConnectorMock, times(1))
                    .createUsers(Mockito.eq(institutionId), Mockito.eq(productId), Mockito.eq(userId), createUserDtoCaptor.capture(), eq("setTitle"));
           createUserDtoCaptor.getValue().getRoles().forEach(role1 -> {
                createUserDto.getRoles().forEach(role -> {
                    if (role.getLabel().equals(role1.getLabel())) {
                        Assertions.assertEquals(role.getPartyRole(), role1.getPartyRole());
                        Assertions.assertEquals(role.getLabel(), role1.getLabel());
                    }
                });
                {
                }
            });
            TestUtils.reflectionEqualsByName(createUserDtoCaptor.getValue(), createUserDto);
            verifyNoMoreInteractions(msCoreConnectorMock);
        } else {
            InvalidProductRoleException e = assertThrows(InvalidProductRoleException.class, executable);
            createUserDto.getRoles().forEach(role -> {
                Assertions.assertEquals(String.format("Product role '%s' is not valid", role.getProductRole()), e.getMessage());
            });
            verifyNoInteractions(msCoreConnectorMock);
        }
        verify(productsConnectorMock, times(1))
                .getProduct(productId);
        verifyNoInteractions(userRegistryConnector);
        verifyNoMoreInteractions(productsConnectorMock);
    }

    @Test
    void getOnboardingRequestInfo() {
        // given
        final String tokenId = "tokenId";
        final OnboardingRequestInfo onboardingRequestInfoMock = mockInstance(new OnboardingRequestInfo(), "setAdmins");
        final UserInfo adminMock = mockInstance(new UserInfo());
        onboardingRequestInfoMock.setAdmins(List.of(adminMock));
        when(msCoreConnectorMock.getOnboardingRequestInfo(any()))
                .thenReturn(onboardingRequestInfoMock);
        User userMock = mockInstance(new User(), "setId");
        WorkContact contact = mockInstance(new WorkContact());
        Map<String, WorkContact> workContact = new HashMap<>();
        workContact.put(onboardingRequestInfoMock.getInstitutionInfo().getId(), contact);
        userMock.setWorkContacts(workContact);
        when(userRegistryConnector.getUserByInternalId(any(), any()))
                .thenAnswer(invocation -> {
                    userMock.setId(invocation.getArgument(0, String.class));
                    return userMock;
                });
        // when
        final OnboardingRequestInfo result = institutionService.getOnboardingRequestInfo(tokenId);
        // then
        assertNotNull(result);
        assertNotNull(result.getManager().getUser());
        assertEquals(result.getManager().getId(), result.getManager().getUser().getId());
        assertNotNull(result.getAdmins().get(0).getUser());
        assertEquals(result.getAdmins().get(0).getId(), result.getAdmins().get(0).getUser().getId());
        verify(msCoreConnectorMock, times(1))
                .getOnboardingRequestInfo(tokenId);
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EnumSet<Fields>> filedsCaptor = ArgumentCaptor.forClass(EnumSet.class);
        verify(userRegistryConnector, times(2))
                .getUserByInternalId(userIdCaptor.capture(), filedsCaptor.capture());
        EnumSet<Fields> capturedFields = filedsCaptor.getValue();
        List<String> userIds = userIdCaptor.getAllValues();
        assertEquals(userIds, List.of(onboardingRequestInfoMock.getManager().getId(), onboardingRequestInfoMock.getAdmins().get(0).getId()));
        assertTrue(capturedFields.contains(name));
        assertTrue(capturedFields.contains(familyName));
        assertTrue(capturedFields.contains(workContacts));
        assertTrue(capturedFields.contains(fiscalCode));
    }

    @Test
    void approveOnboardingRequest() {
        // given
        String tokenId = UUID.randomUUID().toString();
        Mockito.doNothing()
                .when(msCoreConnectorMock).approveOnboardingRequest(anyString());
        // when
        institutionService.approveOnboardingRequest(tokenId);
        // then
        verify(msCoreConnectorMock, times(1))
                .approveOnboardingRequest(tokenId);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void approveOnboardingRequest_hasNullToken() {
        // given
        String tokenId = null;
        // when
        Executable executable = () -> institutionService.approveOnboardingRequest(tokenId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_TOKEN_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreConnectorMock);
    }

    @Test
    void rejectOnboardingRequest(){
        // given
        String tokenId = UUID.randomUUID().toString();
        Mockito.doNothing()
                .when(msCoreConnectorMock).rejectOnboardingRequest(anyString());
        // when
        institutionService.rejectOnboardingRequest(tokenId);
        // then
        verify(msCoreConnectorMock, times(1)).rejectOnboardingRequest(tokenId);
        verifyNoMoreInteractions(msCoreConnectorMock);
    }

    @Test
    void rejectOnboardingRequest_hasNullToken(){
        // given
        String tokenId = null;
        // when
        Executable executable = () -> institutionService.rejectOnboardingRequest(tokenId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_TOKEN_ID_MESSAGE, e.getMessage());
        verifyNoMoreInteractions(msCoreConnectorMock);
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
    void findInstitutionByIdTest2(){
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
    void findInstitutionByIdTest(){
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