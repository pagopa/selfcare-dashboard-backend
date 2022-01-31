package it.pagopa.selfcare.dashboard.connector.rest;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.config.PartyConnectorConfig;
import it.pagopa.selfcare.dashboard.connector.rest.model.*;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.Attribute;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.ACTIVE;
import static it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipState.PENDING;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                PartyConnectorImpl.class,
                PartyConnectorConfig.class
        },
        properties = "USER_STATES_FILTER=ACTIVE,SUSPENDED"
)
class PartyConnectorImplTest {

    private static final Function<PartyRole, SelfCareAuthority> PARTY_2_SELC_ROLE = partyRole -> {
        SelfCareAuthority selfCareRole;
        switch (partyRole) {
            case MANAGER:
            case DELEGATE:
            case SUB_DELEGATE:
                selfCareRole = ADMIN;
                break;
            default:
                selfCareRole = LIMITED;
        }
        return selfCareRole;
    };

    @Autowired
    private PartyConnectorImpl partyConnector;

    @MockBean
    private PartyProcessRestClient restClientMock;

    @Captor
    private ArgumentCaptor<OnboardingRequest> onboardingRequestCaptor;


    @Test
    void getInstitution_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitution_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution_nullAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution_emptyAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setAttributes(Collections.emptyList());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getInstitutionId(), institutionInfo.getInstitutionId());
        assertEquals(onboardingData.getState().toString(), institutionInfo.getStatus());
        assertEquals(onboardingData.getAttributes().get(0).getDescription(), institutionInfo.getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutions() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = TestUtils.mockInstance(new OnboardingData(), 1, "setState");
        onboardingData1.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onboardingData1.setState(ACTIVE);
        OnboardingData onboardingData2 = TestUtils.mockInstance(new OnboardingData(), 2, "setState", "setInstitutionId");
        onboardingData2.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onboardingData2.setState(RelationshipState.PENDING);
        onboardingData2.setInstitutionId(onboardingData1.getInstitutionId());
        OnboardingData onboardingData3 = TestUtils.mockInstance(new OnboardingData(), 3, "setState");
        onboardingData3.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onboardingData3.setState(RelationshipState.PENDING);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3, onboardingData3));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(2, institutions.size());
        Map<String, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getStatus));
        List<InstitutionInfo> institutionInfos = map.get(ACTIVE.name());
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData1.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData1.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData1.getInstitutionId(), institutionInfos.get(0).getInstitutionId());
        assertEquals(onboardingData1.getState().toString(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData1.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        institutionInfos = map.get(RelationshipState.PENDING.name());
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData3.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData3.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData3.getInstitutionId(), institutionInfos.get(0).getInstitutionId());
        assertEquals(onboardingData3.getState().toString(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData3.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(Mockito.isNull(), Mockito.eq(EnumSet.of(ACTIVE, PENDING)));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_nullProducts() {
        // given
        String institutionId = "institutionId";
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_nullProductsInfo() {
        // given
        String institutionId = "institutionId";
        Mockito.when(restClientMock.getInstitutionProducts(Mockito.any(), Mockito.any()))
                .thenReturn(new Products());
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_emptyProductsInfo() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(Collections.emptyList());
        Mockito.when(restClientMock.getInstitutionProducts(Mockito.any(), Mockito.any()))
                .thenReturn(products);
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(List.of(TestUtils.mockInstance(new Product())));
        Mockito.when(restClientMock.getInstitutionProducts(Mockito.any(), Mockito.any()))
                .thenReturn(products);
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertFalse(institutionProducts.isEmpty());
        assertEquals(products.getProducts().get(0).getId(), institutionProducts.get(0).getId());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullProductInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData(), "setProductInfo");
        onboardingData.setState(ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = TestUtils.mockInstance(new OnboardingData(), 1);
        onboardingData1.setState(ACTIVE);
        OnboardingData onboardingData2 = TestUtils.mockInstance(new OnboardingData(), 2, "setProductInfo");
        onboardingData2.setState(ACTIVE);
        OnboardingData onboardingData3 = TestUtils.mockInstance(new OnboardingData(), 3, "setInstitutionId");
        onboardingData3.setInstitutionId(onboardingData1.getInstitutionId());
        onboardingData3.setState(ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3));
        Mockito.when(restClientMock.getOnBoardingInfo(Mockito.any(), Mockito.any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertFalse(authInfos.isEmpty());
        assertEquals(1, authInfos.size());
        AuthInfo authInfo = authInfos.iterator().next();
        assertNotNull(authInfo.getProductRoles());
        assertEquals(2, authInfo.getProductRoles().size());
        authInfo.getProductRoles().forEach(productRole -> {
            if (productRole.getProductId().equals(onboardingData1.getProductInfo().getId())) {
                assertEquals(onboardingData1.getProductInfo().getRole(), productRole.getProductRole());
                assertEquals(PARTY_2_SELC_ROLE.apply(onboardingData1.getRole()), productRole.getSelfCareRole());
            } else if (productRole.getProductId().equals(onboardingData3.getProductInfo().getId())) {
                assertEquals(onboardingData3.getProductInfo().getRole(), productRole.getProductRole());
                assertEquals(PARTY_2_SELC_ROLE.apply(onboardingData3.getRole()), productRole.getSelfCareRole());
            } else {
                Assertions.fail();
            }
        });
        Mockito.verify(restClientMock, Mockito.times(1))
                .getOnBoardingInfo(institutionId, EnumSet.of(ACTIVE));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void party2SelcRoleMapping(PartyRole partyRole) {
        // when
        SelfCareAuthority authority = PartyConnectorImpl.PARTY_ROLE_AUTHORITY_MAP.get(partyRole);
        // then
        assertEquals(PARTY_2_SELC_ROLE.apply(partyRole), authority);
    }


    @Test
    void getUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        // when
        Executable executable = () -> partyConnector.getUsers(institutionId, role, productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void getUsers_nullRole() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = null;
        Optional<String> productId = Optional.empty();
        // when
        Executable executable = () -> partyConnector.getUsers(institutionId, role, productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional role object is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void getUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = null;
        // when
        Executable executable = () -> partyConnector.getUsers(institutionId, role, productId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Optional Product id object is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUsers_nullResponse() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock, null);

        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUsers_notEmptyProductIds() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.of("productId");
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.eq(productId.map(Set::of).get()));
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_notEmptyRole(SelfCareAuthority selfCareAuthority) {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.of(selfCareAuthority);
        Optional<String> productId = Optional.empty();
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        EnumSet<PartyRole> partyRoles = EnumSet.noneOf(PartyRole.class);
        for (PartyRole partyRole : PartyRole.values()) {
            if (role.get().equals(PARTY_2_SELC_ROLE.apply(partyRole))) {
                partyRoles.add(partyRole);
            }
        }
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.eq(partyRoles), Mockito.notNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(userInfo.getRelationshipId());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());
        Assertions.assertEquals(2, userInfo.getProducts().size());
        it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo = userInfo.getProducts().iterator().next();
        Assertions.assertNotNull(productInfo.getId());
        Assertions.assertNull(productInfo.getTitle());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_nullAllowedState() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock, null);

        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(userInfo.getRelationshipId());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());
        Assertions.assertEquals(2, userInfo.getProducts().size());
        it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo = userInfo.getProducts().iterator().next();
        Assertions.assertNotNull(productInfo.getId());
        Assertions.assertNull(productInfo.getTitle());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_emptyAllowedStates() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock, new String[0]);

        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(userInfo.getRelationshipId());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());
        Assertions.assertEquals(2, userInfo.getProducts().size());
        it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo productInfo = userInfo.getProducts().iterator().next();
        Assertions.assertNotNull(productInfo.getId());
        Assertions.assertNull(productInfo.getTitle());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_higherRoleForActiveUsers(SelfCareAuthority selfCareAuthority) {
        //given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setName("user1");
        relationshipInfo1.setRole(PartyRole.OPERATOR);
        relationshipInfo1.setState(ACTIVE);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setName("user2");
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        relationshipInfo2.setState(ACTIVE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user2", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
        Assertions.assertEquals(1, userInfos.size());
    }

    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_higherRoleForPendingUsers(SelfCareAuthority selfCareAuthority) {
        //given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setName("user1");
        relationshipInfo1.setRole(PartyRole.DELEGATE);
        relationshipInfo1.setState(PENDING);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setName("user2");
        relationshipInfo2.setRole(PartyRole.OPERATOR);
        relationshipInfo2.setState(PENDING);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
        Assertions.assertEquals("PENDING", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
    }

    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_activeRoleUserDifferentStatus(SelfCareAuthority selfCareAuthority) {
        //given
        String institutionId = "institutionId";
        Optional<SelfCareAuthority> role = Optional.empty();
        Optional<String> productId = Optional.empty();
        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setName("user1");
        relationshipInfo1.setRole(PartyRole.OPERATOR);
        relationshipInfo1.setState(ACTIVE);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setName("user2");
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        relationshipInfo2.setState(PENDING);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, role, productId);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(LIMITED, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
    }

    @Test
    void createUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void createUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Product id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void createUsers_nullUser() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        CreateUserDto createUserDto = null;
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An User is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void createUsers(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setPartyRole");
        createUserDto.setPartyRole(partyRole.toString());
        // when
        Executable executable = () -> partyConnector.createUsers(institutionId, productId, createUserDto);
        // then
        switch (partyRole) {
            case SUB_DELEGATE:
                Assertions.assertDoesNotThrow(executable);
                Mockito.verify(restClientMock, Mockito.times(1))
                        .onboardingSubdelegates(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor);
                break;
            case OPERATOR:
                Assertions.assertDoesNotThrow(executable);
                Mockito.verify(restClientMock, Mockito.times(1))
                        .onboardingOperators(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor);
                break;
            default:
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
                Assertions.assertEquals("Invalid Party role", e.getMessage());
        }
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    private void verifyRequest(String institutionId, String productId, CreateUserDto createUserDto, ArgumentCaptor<OnboardingRequest> onboardingRequestCaptor) {
        OnboardingRequest request = onboardingRequestCaptor.getValue();
        Assertions.assertNotNull(request);
        Assertions.assertEquals(institutionId, request.getInstitutionId());
        Assertions.assertNull(request.getContract());
        Assertions.assertNotNull(request.getUsers());
        Assertions.assertEquals(1, request.getUsers().size());
        Assertions.assertEquals(createUserDto.getName(), request.getUsers().get(0).getName());
        Assertions.assertEquals(createUserDto.getSurname(), request.getUsers().get(0).getSurname());
        Assertions.assertEquals(createUserDto.getTaxCode(), request.getUsers().get(0).getTaxCode());
        Assertions.assertEquals(createUserDto.getEmail(), request.getUsers().get(0).getEmail());
        Assertions.assertEquals(productId, request.getUsers().get(0).getProduct());
        Assertions.assertEquals(createUserDto.getProductRole(), request.getUsers().get(0).getProductRole());
        Assertions.assertEquals(createUserDto.getPartyRole(), request.getUsers().get(0).getRole().toString());
    }


    @Test
    void suspend_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> partyConnector.suspend(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Relationship id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void suspend() {
        // given
        String relationshipId = "relationshipId";
        // when
        partyConnector.suspend(relationshipId);
        // then
        Mockito.verify(restClientMock, Mockito.times(1))
                .suspendRelationship(relationshipId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void activate_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> partyConnector.activate(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Relationship id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }


    @Test
    void activate() {
        // given
        String relationshipId = "relationshipId";
        // when
        partyConnector.activate(relationshipId);
        // then
        Mockito.verify(restClientMock, Mockito.times(1))
                .activateRelationship(relationshipId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

}