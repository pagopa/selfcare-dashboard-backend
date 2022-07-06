package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.Attribute;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.connector.rest.client.PartyProcessRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import static it.pagopa.selfcare.dashboard.connector.model.user.RelationshipState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                PartyConnectorImpl.class
        }
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
    private ArgumentCaptor<OnboardingUsersRequest> onboardingRequestCaptor;


    @Test
    void getInstitution_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitution_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution_nullAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution_emptyAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setAttributes(Collections.emptyList());
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getOnBoardedInstitution() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData());
        onboardingData.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onBoardingInfo.setInstitutions(Collections.singletonList(onboardingData));
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = partyConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        TestUtils.checkNotNullFields(institutionInfo);
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getExternalId(), institutionInfo.getExternalId());
        assertEquals(onboardingData.getState().toString(), institutionInfo.getStatus());
        assertEquals(onboardingData.getAttributes().get(0).getDescription(), institutionInfo.getCategory());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getOnBoardedInstitutions() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = TestUtils.mockInstance(new OnboardingData(), 1, "setState");
        onboardingData1.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onboardingData1.setState(ACTIVE);
        OnboardingData onboardingData2 = TestUtils.mockInstance(new OnboardingData(), 2, "setState", "setId");
        onboardingData2.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onboardingData2.setState(RelationshipState.PENDING);
        onboardingData2.setId(onboardingData1.getId());
        OnboardingData onboardingData3 = TestUtils.mockInstance(new OnboardingData(), 3, "setState");
        onboardingData3.setAttributes(List.of(TestUtils.mockInstance(new Attribute())));
        onboardingData3.setState(RelationshipState.PENDING);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3, onboardingData3));
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = partyConnector.getOnBoardedInstitutions();
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
        assertEquals(onboardingData1.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData1.getState().toString(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData1.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        institutionInfos = map.get(RelationshipState.PENDING.name());
        assertNotNull(institutionInfos);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData3.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData3.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData3.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData3.getState().toString(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData3.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(isNull(), isNull(), eq(EnumSet.of(ACTIVE, PENDING)));
        verifyNoMoreInteractions(restClientMock);
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
        verify(restClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_nullProductsInfo() {
        // given
        String institutionId = "institutionId";
        when(restClientMock.getInstitutionProducts(any(), any()))
                .thenReturn(new Products());
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(restClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts_emptyProductsInfo() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(Collections.emptyList());
        when(restClientMock.getInstitutionProducts(any(), any()))
                .thenReturn(products);
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(restClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionProducts() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(List.of(TestUtils.mockInstance(new Product())));
        when(restClientMock.getInstitutionProducts(any(), any()))
                .thenReturn(products);
        // when
        List<PartyProduct> institutionProducts = partyConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertFalse(institutionProducts.isEmpty());
        assertEquals(products.getProducts().get(0).getId(), institutionProducts.get(0).getId());
        verify(restClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(restClientMock);
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
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getAuthInfo_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getAuthInfo_nullProductInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = TestUtils.mockInstance(new OnboardingData(), "setProductInfo");
        onboardingData.setState(ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData));
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = partyConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
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
        OnboardingData onboardingData3 = TestUtils.mockInstance(new OnboardingData(), 3, "setId");
        onboardingData3.setId(onboardingData1.getId());
        onboardingData3.setState(ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3));
        when(restClientMock.getOnBoardingInfo(any(), any(), any()))
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
            } else if (productRole.getProductId().equals(onboardingData3.getProductInfo().getId())) {
                assertEquals(onboardingData3.getProductInfo().getRole(), productRole.getProductRole());
            } else {
                fail();
            }
        });
        verify(restClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(restClientMock);
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
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds_emptyProductRole_emptyUserId() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), notNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_nullResponse() {
        // given
        PartyConnectorImpl partyConnector = new PartyConnectorImpl(restClientMock);

        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyProductIds() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of("productId"));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), notNull(), eq(userInfoFilter.getProductId().map(Set::of).get()), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers_notEmptyProductRoles() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductRoles(Optional.of(Set.of("api", "security")));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNotNull(), isNull(), eq(userInfoFilter.getProductRoles().get()), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @ParameterizedTest
    @EnumSource(value = SelfCareAuthority.class)
    void getUsers_notEmptyRole(SelfCareAuthority selfCareAuthority) {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setRole(Optional.of(selfCareAuthority));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));
        // when
        Collection<UserInfo> users = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        EnumSet<PartyRole> partyRoles = EnumSet.noneOf(PartyRole.class);
        for (PartyRole partyRole : PartyRole.values()) {
            if (userInfoFilter.getRole().get().equals(PARTY_2_SELC_ROLE.apply(partyRole))) {
                partyRoles.add(partyRole);
            }
        }
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), eq(partyRoles), isNotNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(userInfos);
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        ProductInfo product = productInfoMap.get(prodId);
        assertEquals(id, userInfo.getId());
        assertNotNull(product.getRoleInfos());
        assertNotNull(product.getId());
        assertNull(product.getTitle());
        assertNull(userInfo.getUser());
        assertNotNull(userInfo.getStatus());
        assertNotNull(userInfo.getRole());
        assertEquals(1, userInfo.getProducts().size());

        assertNotNull(productInfoMap.keySet());
        verify(restClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), notNull(), isNull(), isNull(), any());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getUser() {
        // given
        String relationshipId = "relationshipId";

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo1.setFrom(relationshipId);
        when(restClientMock.getRelationship(anyString()))
                .thenReturn(relationshipInfo1);
        // when
        UserInfo userInfo = partyConnector.getUser(relationshipId);
        // then
        assertNotNull(userInfo);
        assertEquals(relationshipId, userInfo.getId());
        assertNull(userInfo.getUser());
        assertNotNull(userInfo.getStatus());
        assertNotNull(userInfo.getRole());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        assertEquals(1, userInfo.getProducts().size());
        ProductInfo product = productInfoMap.get(prodId);
        assertNotNull(product.getRoleInfos());
        assertNotNull(product.getId());
        assertNull(product.getTitle());
        verify(restClientMock, times(1))
                .getRelationship(anyString());
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void relationship_info_to_user_info_function() throws IOException {
        // given
        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/relationInfo-to-userInfo.json");
        RelationshipInfo relationshipInfo = mapper.readValue(stub, RelationshipInfo.class);
        // when
        UserInfo userInfo = PartyConnectorImpl.RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
        // then
        assertNull(userInfo.getUser());
        assertEquals(relationshipInfo.getState().toString(), userInfo.getStatus());
        assertEquals(relationshipInfo.getFrom(), userInfo.getId());
        String prodId = null;
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        for (String key :
                productInfoMap.keySet()) {
            prodId = key;
        }
        ProductInfo product = productInfoMap.get(prodId);
        assertEquals(relationshipInfo.getProduct().getId(), product.getId());
        assertEquals(1, product.getRoleInfos().size());
        RoleInfo roleInfo = product.getRoleInfos().get(0);
        assertEquals(relationshipInfo.getProduct().getRole(), product.getRoleInfos().get(0).getRole());
        assertEquals(relationshipInfo.getId(), roleInfo.getRelationshipId());
        assertEquals(ADMIN, roleInfo.getSelcRole());

    }

    @Test
    void getUser_mergeRoleInfos() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/multi-role.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        Map<String, ProductInfo> productInfoMap = userInfo.getProducts();
        assertEquals(2, productInfoMap.values().size());
        assertEquals(2, productInfoMap.get("prod-io").getRoleInfos().size());
        assertEquals(1, productInfoMap.get("prod-pn").getRoleInfos().size());

    }

    @Test
    void getUsers_higherRoleForActiveUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-active.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //Then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(2, userInfo.getProducts().size());
    }

    @Test
    void getUser_getProductFromMerge() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/merge.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        //then
        assertEquals(1, userInfos.size());
        UserInfo userInfo = userInfos.iterator().next();
        assertEquals(relationshipsResponse.size(), userInfo.getProducts().size());
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());

    }

    @Test
    void getUsers_higherRoleForPendingUsers() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();


        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/higher-role-pending.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);

        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("PENDING", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/active-role-different-status.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(LIMITED, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }


    @Test
    void getUsers_activeRoleUserDifferentStatus_2() throws IOException {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        File stub = ResourceUtils.getFile("classpath:stubs/PartyConnectorImplTest/getUserInstitutionRelationships/active-role-different-status-2.json");
        RelationshipsResponse relationshipsResponse = mapper.readValue(stub, RelationshipsResponse.class);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void getUsers_activeRoleUserDifferentStatus2() {
        //given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        RelationshipInfo relationshipInfo1 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setRole(PartyRole.OPERATOR);
        relationshipInfo1.setState(PENDING);
        RelationshipInfo relationshipInfo2 = TestUtils.mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        relationshipInfo2.setState(ACTIVE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(restClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = partyConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());
    }

    @Test
    void createUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void createUsers_nullProductId() {
        // given
        String institutionId = "institutionId";
        String productId = null;
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void createUsers_nullUser() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = null;
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void createUsers_nullUserId() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = null;
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> {
            partyConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An User Id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void createUsers(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = TestUtils.mockInstance(new CreateUserDto.Role(), "setPartyROle");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        // when
        Executable executable = () -> partyConnector.createUsers(institutionId, productId, userId, createUserDto);
        // then
        switch (partyRole) {
            case SUB_DELEGATE:
                assertDoesNotThrow(executable);
                verify(restClientMock, times(1))
                        .onboardingSubdelegates(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor, userId);
                break;
            case OPERATOR:
                assertDoesNotThrow(executable);
                verify(restClientMock, times(1))
                        .onboardingOperators(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor, userId);
                break;
            default:
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
                assertEquals("Invalid Party role", e.getMessage());
        }
        verifyNoMoreInteractions(restClientMock);
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
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = TestUtils.mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock1 = TestUtils.mockInstance(new CreateUserDto.Role(), "setPartyROle");
        CreateUserDto.Role roleMock2 = TestUtils.mockInstance(new CreateUserDto.Role(), "setPartyROle");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        roleMock2.setProductRole(productRoles2);
        roleMock2.setPartyRole(partyRole2);
        createUserDto.setRoles(Set.of(roleMock1, roleMock2));
        // when
        Executable executable = () -> partyConnector.createUsers(institutionId, productId, userId, createUserDto);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Is not allowed to create both SUB_DELEGATE and OPERATOR users", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    private void verifyRequest(String institutionId, String productId, CreateUserDto createUserDto, ArgumentCaptor<OnboardingUsersRequest> onboardingRequestCaptor, String userId) {
        OnboardingUsersRequest request = onboardingRequestCaptor.getValue();
        assertNotNull(request);
        assertEquals(institutionId, request.getInstitutionId());
        assertEquals(productId, request.getProductId());
        assertNotNull(request.getUsers());
        assertEquals(1, request.getUsers().size());
        assertEquals(createUserDto.getName(), request.getUsers().get(0).getName());
        assertEquals(createUserDto.getSurname(), request.getUsers().get(0).getSurname());
        assertEquals(createUserDto.getTaxCode(), request.getUsers().get(0).getTaxCode());
        assertEquals(createUserDto.getEmail(), request.getUsers().get(0).getEmail());
        assertEquals(userId, request.getUsers().get(0).getId().toString());
        createUserDto.getRoles().forEach(role -> request.getUsers().forEach(user -> {
            assertEquals(role.getProductRole(), user.getProductRole());
            assertEquals(role.getPartyRole(), user.getRole());
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
        assertEquals("A Relationship id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void suspend() {
        // given
        String relationshipId = "relationshipId";
        // when
        partyConnector.suspend(relationshipId);
        // then
        verify(restClientMock, times(1))
                .suspendRelationship(relationshipId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void activate_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> partyConnector.activate(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Relationship id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void activate() {
        // given
        String relationshipId = "relationshipId";
        // when
        partyConnector.activate(relationshipId);
        // then
        verify(restClientMock, times(1))
                .activateRelationship(relationshipId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void delete_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> partyConnector.delete(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Relationship id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }

    @Test
    void delete() {
        // given
        String relationshipId = "relationshipId";
        // when
        partyConnector.delete(relationshipId);
        // then
        verify(restClientMock, times(1))
                .deleteRelationshipById(relationshipId);
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void userInfoFilter_emptyOptionals() {
        //given
        Optional<Set<String>> productRoles = null;
        Optional<String> userId = null;
        Optional<SelfCareAuthority> role = null;
        Optional<String> productId = null;
        Optional<EnumSet<RelationshipState>> allowedStates = null;
        //when
        UserInfo.UserInfoFilter filter = new UserInfo.UserInfoFilter();
        filter.setUserId(userId);
        filter.setProductRoles(productRoles);
        filter.setProductId(productId);
        filter.setRole(role);
        filter.setAllowedState(allowedStates);
        //then
        assertEquals(Optional.empty(), filter.getProductId());
        assertEquals(Optional.empty(), filter.getProductRoles());
        assertEquals(Optional.empty(), filter.getUserId());
        assertEquals(Optional.empty(), filter.getRole());
        assertEquals(Optional.empty(), filter.getAllowedStates());
    }

    @Test
    void getInstitution_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> partyConnector.getInstitution(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }


    @Test
    void getInstitution_nullResponse() {
        // given
        String institutionId = "institutionId";
        // when
        Institution institution = partyConnector.getInstitution(institutionId);
        // then
        assertNull(institution);
        verify(restClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Attribute attribute = TestUtils.mockInstance(new Attribute());
        institutionMock.setAttributes(List.of(attribute));
        when(restClientMock.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = partyConnector.getInstitution(institutionId);
        // then
        assertSame(institutionMock, institution);
        TestUtils.checkNotNullFields(institution);
        verify(restClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(restClientMock);
    }

    @Test
    void getInstitutionByExternalId_nullInstitutionId() {
        // given
        String institutionExternalId = null;
        // when
        Executable executable = () -> partyConnector.getInstitutionByExternalId(institutionExternalId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution external id is required", e.getMessage());
        verifyNoInteractions(restClientMock);
    }


    @Test
    void getInstitutionByExternalId_nullResponse() {
        // given
        String institutionExternalId = "institutionExternalId";
        // when
        Institution institution = partyConnector.getInstitutionByExternalId(institutionExternalId);
        // then
        assertNull(institution);
        verify(restClientMock, times(1))
                .getInstitutionByExternalId(institutionExternalId);
        verifyNoMoreInteractions(restClientMock);
    }


    @Test
    void getInstitutionByExternalId() {
        // given
        String institutionExternalId = "institutionExternalId";
        Institution institutionMock = TestUtils.mockInstance(new Institution());
        Attribute attribute = TestUtils.mockInstance(new Attribute());
        institutionMock.setAttributes(List.of(attribute));
        when(restClientMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = partyConnector.getInstitutionByExternalId(institutionExternalId);
        // then
        assertSame(institutionMock, institution);
        TestUtils.checkNotNullFields(institution);
        verify(restClientMock, times(1))
                .getInstitutionByExternalId(institutionExternalId);
        verifyNoMoreInteractions(restClientMock);
    }

}