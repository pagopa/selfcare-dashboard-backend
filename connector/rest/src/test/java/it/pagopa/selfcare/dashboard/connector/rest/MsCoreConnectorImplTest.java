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
import it.pagopa.selfcare.dashboard.connector.model.auth.AuthInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.mapper.BrokerMapper;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.Relationship;
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;
import static it.pagopa.selfcare.commons.utils.TestUtils.*;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static it.pagopa.selfcare.dashboard.connector.rest.MsCoreConnectorImpl.REQUIRED_INSTITUTION_ID_MESSAGE;
import static it.pagopa.selfcare.dashboard.connector.rest.MsCoreConnectorImpl.REQUIRED_UPDATE_RESOURCE_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                MsCoreConnectorImpl.class
        }
)
class MsCoreConnectorImplTest {

    private final ObjectMapper mapper;

    public MsCoreConnectorImplTest() {
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
    private MsCoreConnectorImpl msCoreConnector;

    @MockBean
    private MsCoreRestClient msCoreRestClientMock;

    @MockBean
    private BrokerMapper brokerMapper;

    @Captor
    private ArgumentCaptor<OnboardingUsersRequest> onboardingRequestCaptor;

    @Test
    void getOnBoardedInstitutions_toBeValidatedtoBeValidate() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = mockInstance(new OnboardingData(), 1, "setState");
        onboardingData1.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData1.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData1.setState(RelationshipState.TOBEVALIDATED);
        OnboardingData onboardingData2 = mockInstance(new OnboardingData(), 2, "setState", "setId");
        onboardingData2.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData2.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData2.setState(RelationshipState.TOBEVALIDATED);
        onboardingData2.setId(onboardingData1.getId());
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = msCoreConnector.getOnBoardedInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        Map<RelationshipState, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getStatus));
        List<InstitutionInfo> institutionInfos = map.get(RelationshipState.TOBEVALIDATED);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData2.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData2.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData2.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData2.getState(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData2.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        assertEquals(onboardingData2.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData2.getGeographicTaxonomies().get(0).getCode(), institutionInfos.get(0).getGeographicTaxonomies().get(0).getCode());
        assertEquals(onboardingData2.getGeographicTaxonomies().get(0).getDesc(), institutionInfos.get(0).getGeographicTaxonomies().get(0).getDesc());
        reflectionEqualsByName(onboardingData2.getPaymentServiceProvider(), institutionInfos.get(0).getPaymentServiceProvider());
        reflectionEqualsByName(onboardingData2.getSupportContact(), institutionInfos.get(0).getSupportContact());
        reflectionEqualsByName(onboardingData2.getBilling(), institutionInfos.get(0).getBilling());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), isNull(), eq(EnumSet.of(ACTIVE, PENDING, TOBEVALIDATED)));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getOnBoardedInstitutions_pendingToBeValidated() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = mockInstance(new OnboardingData(), 1, "setState");
        onboardingData1.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData1.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData1.setState(RelationshipState.PENDING);
        OnboardingData onboardingData2 = mockInstance(new OnboardingData(), 2, "setState", "setId");
        onboardingData2.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData2.setState(RelationshipState.TOBEVALIDATED);
        onboardingData2.setId(onboardingData1.getId());
        onboardingData2.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = msCoreConnector.getOnBoardedInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        Map<RelationshipState, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getStatus));
        List<InstitutionInfo> institutionInfos = map.get(RelationshipState.PENDING);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData1.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData1.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData1.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData1.getState(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData1.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        assertEquals(onboardingData1.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData1.getGeographicTaxonomies().get(0).getCode(), institutionInfos.get(0).getGeographicTaxonomies().get(0).getCode());
        assertEquals(onboardingData1.getGeographicTaxonomies().get(0).getDesc(), institutionInfos.get(0).getGeographicTaxonomies().get(0).getDesc());
        reflectionEqualsByName(onboardingData1.getPaymentServiceProvider(), institutionInfos.get(0).getPaymentServiceProvider());
        reflectionEqualsByName(onboardingData1.getSupportContact(), institutionInfos.get(0).getSupportContact());
        reflectionEqualsByName(onboardingData1.getBilling(), institutionInfos.get(0).getBilling());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), isNull(), eq(EnumSet.of(ACTIVE, PENDING, TOBEVALIDATED)));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getOnBoardedInstitutions_activePendingToBeValidated() {
        // given
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = mockInstance(new OnboardingData(), 1, "setState");
        onboardingData1.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData1.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData1.setState(RelationshipState.ACTIVE);
        OnboardingData onboardingData2 = mockInstance(new OnboardingData(), 2, "setState", "setId");
        onboardingData2.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData2.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData2.setState(RelationshipState.PENDING);
        onboardingData2.setId(onboardingData1.getId());
        OnboardingData onboardingData3 = mockInstance(new OnboardingData(), 3, "setState", "setId");
        onboardingData3.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData3.setState(RelationshipState.TOBEVALIDATED);
        onboardingData3.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onboardingData3.setId(onboardingData1.getId());
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<InstitutionInfo> institutions = msCoreConnector.getOnBoardedInstitutions();
        // then
        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        Map<RelationshipState, List<InstitutionInfo>> map = institutions.stream()
                .collect(Collectors.groupingBy(InstitutionInfo::getStatus));
        List<InstitutionInfo> institutionInfos = map.get(RelationshipState.ACTIVE);
        assertEquals(1, institutionInfos.size());
        assertEquals(onboardingData1.getDescription(), institutionInfos.get(0).getDescription());
        assertEquals(onboardingData1.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData1.getExternalId(), institutionInfos.get(0).getExternalId());
        assertEquals(onboardingData1.getState(), institutionInfos.get(0).getStatus());
        assertEquals(onboardingData1.getAttributes().get(0).getDescription(), institutionInfos.get(0).getCategory());
        assertEquals(onboardingData1.getDigitalAddress(), institutionInfos.get(0).getDigitalAddress());
        assertEquals(onboardingData1.getGeographicTaxonomies().get(0).getCode(), institutionInfos.get(0).getGeographicTaxonomies().get(0).getCode());
        assertEquals(onboardingData1.getGeographicTaxonomies().get(0).getDesc(), institutionInfos.get(0).getGeographicTaxonomies().get(0).getDesc());
        reflectionEqualsByName(onboardingData1.getPaymentServiceProvider(), institutionInfos.get(0).getPaymentServiceProvider());
        reflectionEqualsByName(onboardingData1.getSupportContact(), institutionInfos.get(0).getSupportContact());
        reflectionEqualsByName(onboardingData1.getBilling(), institutionInfos.get(0).getBilling());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(isNull(), isNull(), eq(EnumSet.of(ACTIVE, PENDING, TOBEVALIDATED)));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getInstitutionProducts_nullProducts() {
        // given
        String institutionId = "institutionId";
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitutionProducts_nullProductsInfo() {
        // given
        String institutionId = "institutionId";
        when(msCoreRestClientMock.getInstitutionProducts(any(), any()))
                .thenReturn(new Products());
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitutionProducts_emptyProductsInfo() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(Collections.emptyList());
        when(msCoreRestClientMock.getInstitutionProducts(any(), any()))
                .thenReturn(products);
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertTrue(institutionProducts.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitutionProducts() {
        // given
        String institutionId = "institutionId";
        Products products = new Products();
        products.setProducts(List.of(mockInstance(new Product())));
        when(msCoreRestClientMock.getInstitutionProducts(any(), any()))
                .thenReturn(products);
        // when
        List<PartyProduct> institutionProducts = msCoreConnector.getInstitutionProducts(institutionId);
        // then
        assertNotNull(institutionProducts);
        assertFalse(institutionProducts.isEmpty());
        assertEquals(products.getProducts().get(0).getId(), institutionProducts.get(0).getId());
        verify(msCoreRestClientMock, times(1))
                .getInstitutionProducts(institutionId, EnumSet.allOf(ProductState.class));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getAuthInfo_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getAuthInfo_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getAuthInfo_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getAuthInfo_nullProductInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setProductInfo");
        onboardingData.setState(ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
        // then
        assertNotNull(authInfos);
        assertTrue(authInfos.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getAuthInfo() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData1 = mockInstance(new OnboardingData(), 1);
        onboardingData1.setState(ACTIVE);
        OnboardingData onboardingData2 = mockInstance(new OnboardingData(), 2, "setProductInfo");
        onboardingData2.setState(ACTIVE);
        OnboardingData onboardingData3 = mockInstance(new OnboardingData(), 3, "setId");
        onboardingData3.setId(onboardingData1.getId());
        onboardingData3.setState(ACTIVE);
        onBoardingInfo.setInstitutions(List.of(onboardingData1, onboardingData2, onboardingData3));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Collection<AuthInfo> authInfos = msCoreConnector.getAuthInfo(institutionId);
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
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
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
        Executable executable = () -> msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void getUsers_nullResponse_emptyRole_emptyProductIds_emptyProductRole_emptyUserId() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), notNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getUsers_nullResponse() {
        // given
        MsCoreConnectorImpl msCoreConnector = new MsCoreConnectorImpl(msCoreRestClientMock, brokerMapper);

        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();

        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getUsers_notEmptyProductIds() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(Optional.of("productId"));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), notNull(), eq(userInfoFilter.getProductId().map(Set::of).get()), isNull(), isNull());
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getUsers_notEmptyProductRoles() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductRoles(Optional.of(Set.of("api", "security")));
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        // when
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), isNotNull(), isNull(), eq(userInfoFilter.getProductRoles().get()), isNull());
        verifyNoMoreInteractions(msCoreRestClientMock);
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
        Collection<UserInfo> users = msCoreConnector.getUsers(institutionId, userInfoFilter);
        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        EnumSet<PartyRole> partyRoles = EnumSet.noneOf(PartyRole.class);
        for (PartyRole partyRole : PartyRole.values()) {
            if (userInfoFilter.getRole().get().equals(PARTY_2_SELC_ROLE.apply(partyRole))) {
                partyRoles.add(partyRole);
            }
        }
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), eq(partyRoles), isNotNull(), isNull(), isNull(), isNull());
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getUsers() {
        // given
        String institutionId = "institutionId";
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE, SUSPENDED)));

        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        RelationshipInfo relationshipInfo2 = mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        // when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(eq(institutionId), isNull(), notNull(), isNull(), isNull(), any());
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getUser() {
        // given
        String relationshipId = "relationshipId";

        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo1.setFrom(relationshipId);
        when(msCoreRestClientMock.getRelationship(anyString()))
                .thenReturn(relationshipInfo1);
        // when
        UserInfo userInfo = msCoreConnector.getUser(relationshipId);
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
        verify(msCoreRestClientMock, times(1))
                .getRelationship(anyString());
        verifyNoMoreInteractions(msCoreRestClientMock);
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
        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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

        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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

        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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

        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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
        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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
        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
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

        RelationshipInfo relationshipInfo1 = mockInstance(new RelationshipInfo(), "setFrom");
        String id = "id";
        relationshipInfo1.setFrom(id);
        relationshipInfo1.setRole(PartyRole.OPERATOR);
        relationshipInfo1.setState(PENDING);
        RelationshipInfo relationshipInfo2 = mockInstance(new RelationshipInfo(), "setFrom");
        relationshipInfo2.setFrom(id);
        relationshipInfo2.setRole(PartyRole.DELEGATE);
        relationshipInfo2.setState(ACTIVE);
        RelationshipsResponse relationshipsResponse = new RelationshipsResponse();
        relationshipsResponse.add(relationshipInfo1);
        relationshipsResponse.add(relationshipInfo2);
        when(msCoreRestClientMock.getUserInstitutionRelationships(any(), any(), any(), any(), any(), any()))
                .thenReturn(relationshipsResponse);
        //when
        Collection<UserInfo> userInfos = msCoreConnector.getUsers(institutionId, userInfoFilter);
        UserInfo userInfo = userInfos.iterator().next();
        //Then
        assertNull(userInfo.getUser());
        assertEquals(ADMIN, userInfo.getRole());
        assertEquals("ACTIVE", userInfo.getStatus());
        assertEquals(1, userInfos.size());

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
        Executable executable = () -> msCoreConnector.getInstitution(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitution_nullResponse() {
        // given
        String institutionId = "institutionId";
        // when
        Institution institution = msCoreConnector.getInstitution(institutionId);
        // then
        assertNull(institution);
        verify(msCoreRestClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(msCoreRestClientMock);

    }


    @Test
    void getInstitution() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionMock.setAttributes(List.of(attribute));
        when(msCoreRestClientMock.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = msCoreConnector.getInstitution(institutionId);
        // then
        assertSame(institutionMock, institution);
        checkNotNullFields(institution);
        verify(msCoreRestClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(msCoreRestClientMock);

    }

    @Test
    void relationshipToInstitutionInfoFunction() {
        // given
        Relationship relationshipMock = mockInstance(new Relationship());
        // when
        final InstitutionInfo result = PartyConnectorImpl.RELATIONSHIP_TO_INSTITUTION_INFO_FUNCTION.apply(relationshipMock);
        // then
        assertEquals(relationshipMock.getInstitutionUpdate().getInstitutionType(), result.getInstitutionType());
        assertEquals(relationshipMock.getInstitutionUpdate().getDescription(), result.getDescription());
        assertEquals(relationshipMock.getInstitutionUpdate().getTaxCode(), result.getTaxCode());
        assertEquals(relationshipMock.getInstitutionUpdate().getDigitalAddress(), result.getDigitalAddress());
        assertEquals(relationshipMock.getInstitutionUpdate().getAddress(), result.getAddress());
        assertEquals(relationshipMock.getInstitutionUpdate().getZipCode(), result.getZipCode());
        assertEquals(relationshipMock.getInstitutionUpdate().getPaymentServiceProvider(), result.getPaymentServiceProvider());
        assertEquals(relationshipMock.getInstitutionUpdate().getDataProtectionOfficer(), result.getDataProtectionOfficer());
        assertEquals(relationshipMock.getBilling(), result.getBilling());
    }


    @Test
    void updateInstitutionDescription() {
        // given
        String institutionId = "setId";
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreRestClientMock.updateInstitutionDescription(anyString(), any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = msCoreConnector.updateInstitutionDescription(institutionId, resource);
        // then
        assertEquals(institution.getId(), institutionId);
        assertEquals(institution.getDescription(), resource.getDescription());
        assertEquals(institution.getDigitalAddress(), resource.getDigitalAddress());
        verify(msCoreRestClientMock, times(1))
                .updateInstitutionDescription(institutionId, resource);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void updateGeographicTaxonomy_hasNullInstitutionId() {
        // given
        String institutionId = null;
        UpdateInstitutionResource resource = mockInstance(new UpdateInstitutionResource());
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionDescription(institutionId, resource);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void updateGeographicTaxonomy_hasNullGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        UpdateInstitutionResource resource = null;
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionDescription(institutionId, resource);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_UPDATE_RESOURCE_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

}