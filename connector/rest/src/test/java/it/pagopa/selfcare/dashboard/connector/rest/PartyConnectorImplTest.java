package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.PartyRole;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
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
import org.springframework.util.ResourceUtils;

import javax.validation.ValidationException;
import java.io.File;
import java.io.IOException;
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

    private final ObjectMapper mapper;

    public PartyConnectorImplTest() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
    }

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
        SelfCareAuthority authority = partyRole.getSelfCareAuthority();
        // then
        assertEquals(PARTY_2_SELC_ROLE.apply(partyRole), authority);
    }

    @Test
    void getUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        // when
        Executable executable = () -> partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("An Institution id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds_emptyProductRole_emptyUserId() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_nullResponse() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock, null);

        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyProductIds() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of("productId"));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.eq(userInfoFilter.getProductId().map(Set::of).get()), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyProductRoles() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductRoles(Optional.of(Set.of("api", "security")));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNotNull(), Mockito.isNull(), Mockito.eq(userInfoFilter.getProductRoles().get()), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_notEmptyRole(SelfCareAuthority selfCareAuthority) {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(Optional.of(selfCareAuthority));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(users);
        Assertions.assertTrue(users.isEmpty());
        EnumSet<PartyRole> partyRoles = EnumSet.noneOf(PartyRole.class);
        for (PartyRole partyRole : PartyRole.values()) {
            if (userInfoFilter.getRole().get().equals(PARTY_2_SELC_ROLE.apply(partyRole))) {
                partyRoles.add(partyRole);
            }
        }
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.eq(partyRoles), Mockito.notNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        ProductInfo product = productInfoMap.get(prodId);
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(product.getRoleInfos());
        Assertions.assertNotNull(product.getId());
        Assertions.assertNull(product.getTitle());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());
        Assertions.assertEquals(1, userInfo.getProducts().size());

        Assertions.assertNotNull(productInfoMap.keySet());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.notNull(), Mockito.isNull(), Mockito.isNull(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_nullAllowedState() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock, null);

        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        Assertions.assertEquals(1, userInfo.getProducts().size());
        ProductInfo product = productInfoMap.get(prodId);
        Assertions.assertNotNull(product.getRoleInfos());
        Assertions.assertNotNull(product.getId());
        Assertions.assertNull(product.getTitle());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.any());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_emptyAllowedStates() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock, new String[0]);

        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        Assertions.assertNotNull(userInfos);
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(id, userInfo.getId());
        Assertions.assertNotNull(userInfo.getName());
        Assertions.assertNotNull(userInfo.getSurname());
        Assertions.assertNotNull(userInfo.getEmail());
        Assertions.assertNotNull(userInfo.getStatus());
        Assertions.assertNotNull(userInfo.getRole());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        Assertions.assertEquals(1, userInfo.getProducts().size());
        ProductInfo product = productInfoMap.get(prodId);
        Assertions.assertNotNull(product.getRoleInfos());
        Assertions.assertNotNull(product.getId());
        Assertions.assertNull(product.getTitle());
        Mockito.verify(restClientMock, Mockito.times(1))
                .getInstitutionRelationships(Mockito.eq(institutionId), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
        Mockito.verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void relationship_info_to_user_info_function() throws IOException {
        // given
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/relationInfo-to-userInfo.json");
        RelationshipInfo relationshipInfo = mapper.readValue(stub, RelationshipInfo.class);
        // when
        UserInfo userInfo = PartyConnectorImpl.RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        // then
        Assertions.assertEquals(relationshipInfo.getName(), userInfo.getName());
        Assertions.assertEquals(relationshipInfo.getSurname(), userInfo.getSurname());
        Assertions.assertEquals(relationshipInfo.getState().toString(), userInfo.getStatus());
        Assertions.assertEquals(relationshipInfo.getEmail(), userInfo.getEmail());
        Assertions.assertEquals(relationshipInfo.getFrom(), userInfo.getId());
        Assertions.assertEquals(relationshipInfo.getTaxCode(), userInfo.getTaxCode());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        ProductInfo product = productInfoMap.get(prodId);
        Assertions.assertEquals(relationshipInfo.getProduct().getId(), product.getId());
        Assertions.assertEquals(1, product.getRoleInfos().size());
        RoleInfo roleInfo = product.getRoleInfos().get(0);
        Assertions.assertEquals(relationshipInfo.getProduct().getRole(), product.getRoleInfos().get(0).getRole());
        Assertions.assertEquals(relationshipInfo.getId(), roleInfo.getRelationshipId());
        Assertions.assertEquals(ADMIN, roleInfo.getSelcRole());

    }

    @Test
    void getUser_mergeRoleInfos() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/multi-role.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //then
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        Assertions.assertEquals(2, productInfoMap.values().size());
        Assertions.assertEquals(2, productInfoMap.get("prod-io").getRoleInfos().size());
        Assertions.assertEquals(1, productInfoMap.get("prod-pn").getRoleInfos().size());

    }

    @Test
    void getUsers_higherRoleForActiveUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/higher-role-active.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //Then
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
        Assertions.assertEquals(2, userInfo.getProducts().size());
    }

    @Test
    void getUser_getProductFromMerge() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/merge.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //then
        Assertions.assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Assertions.assertEquals(relationshipsResponse.size(), userInfo.getProducts().size());
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
        Assertions.assertEquals("PENDING", userInfo.getStatus());

    }

    @Test
    void getUsers_higherRoleForPendingUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/higher-role-pending.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
        Assertions.assertEquals("PENDING", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/active-role-different-status.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(LIMITED, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus_2() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getInstitutionRelationships/active-role-different-status-2.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
        Assertions.assertEquals("ACTIVE", userInfo.getStatus());
        Assertions.assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus2() {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setName("user1");
        relationshipInfo1.setRole(PartyRole.OPERATOR);
        relationshipInfo1.setState(PENDING);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setName("user1");
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        relationshipInfo2.setState(ACTIVE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        Mockito.when(restClientMock.getInstitutionRelationships(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        Assertions.assertEquals("user1", userInfo.getName());
        Assertions.assertEquals(ADMIN, userInfo.getRole());
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
        String productRoles = "Operator Api";
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = TestUtils.mockInstance(new CreateUserDto.Role(), "setPartyROle");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
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

    @Test
    void createUser_multiplePartyRole() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles1 = "Operator Api";
        String productRoles2 = "Operator Security";
        PartyRole partyRole1 = PartyRole.OPERATOR;
        PartyRole partyRole2 = PartyRole.SUB_DELEGATE;
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock1 = TestUtils.mockInstance(new CreateUserDto.Role(), "setPartyROle");
        CreateUserDto.Role roleMock2 = TestUtils.mockInstance(new CreateUserDto.Role(), "setPartyROle");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        roleMock2.setProductRole(productRoles2);
        roleMock2.setPartyRole(partyRole2);
        createUserDto.setRoles(Set.of(roleMock1, roleMock2));
        // when
        Executable executable = () -> partyConnector.createUsers(institutionId, productId, createUserDto);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals("Is not allowed to create both SUB_DELEGATE and OPERATOR users", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
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

        createUserDto.getRoles().forEach(role -> request.getUsers().forEach(user -> {
            Assertions.assertEquals(role.getProductRole(), user.getProductRole());
            Assertions.assertEquals(role.getPartyRole(), user.getRole());
        }));
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

    @Test
    void delete_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> partyConnector.delete(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A Relationship id is required", e.getMessage());
        Mockito.verifyNoInteractions(restClientMock);
    }

    @Test
    void delete() {
        // given
        String relationshipId = "relationshipId";
        // when
        partyConnector.delete(relationshipId);
        // then
        Mockito.verify(restClientMock, Mockito.times(1))
                .deleteRelationshipById(relationshipId);
        Mockito.verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void userInfoFilter_emptyOptionals() {
        //given
        Optional<Set<String>> productRoles = null;
        Optional<String> userId = null;
        Optional<SelfCareAuthority> role = null;
        Optional<String> productId = null;
        //when
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setUserId(userId);
        filter.setProductRoles(productRoles);
        filter.setProductId(productId);
        filter.setRole(role);
        //then
        assertEquals(Optional.empty(), filter.getProductId());
        assertEquals(Optional.empty(), filter.getProductRoles());
        assertEquals(Optional.empty(), filter.getUserId());
        assertEquals(Optional.empty(), filter.getRole());

    }

}