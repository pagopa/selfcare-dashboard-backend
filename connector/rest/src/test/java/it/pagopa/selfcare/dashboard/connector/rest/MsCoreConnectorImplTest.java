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
import it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.connector.onboarding.OnboardingRequestInfo;
import it.pagopa.selfcare.dashboard.connector.rest.client.MsCoreRestClient;
import it.pagopa.selfcare.dashboard.connector.rest.model.InstitutionPut;
import it.pagopa.selfcare.dashboard.connector.rest.model.ProductState;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.RelationshipsResponse;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnBoardingInfo;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingData;
import it.pagopa.selfcare.dashboard.connector.rest.model.onboarding.OnboardingUsersRequest;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Product;
import it.pagopa.selfcare.dashboard.connector.rest.model.product.Products;
import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.Relationship;
import it.pagopa.selfcare.dashboard.connector.rest.model.relationship.RelationshipBinding;
import it.pagopa.selfcare.dashboard.connector.rest.model.token.TokenInfo;
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
import static it.pagopa.selfcare.commons.utils.TestUtils.*;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.*;
import static it.pagopa.selfcare.dashboard.connector.rest.MsCoreConnectorImpl.*;
import static java.util.Collections.singletonList;
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

    @Captor
    private ArgumentCaptor<OnboardingUsersRequest> onboardingRequestCaptor;


    @Test
    void getInstitution_nullOnBoardingInfo() {
        // given
        String institutionId = "institutionId";
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitution_nullInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getInstitution_emptyInstitutions() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        onBoardingInfo.setInstitutions(Collections.emptyList());
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNull(institutionInfo);
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitution_nullAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getInstitution_emptyAttributes() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setAttributes(Collections.emptyList());
        onboardingData.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        assertNull(institutionInfo.getCategory());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getInstitution_nullGeographicTaxonomy() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = mockInstance(new OnboardingData(), "setGeographicTaxonomies");
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        Executable executable = () -> msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("The institution %s does not have geographic taxonomies.", onboardingData.getId()), e.getMessage());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getOnBoardedInstitution() {
        // given
        String institutionId = "institutionId";
        OnBoardingInfo onBoardingInfo = new OnBoardingInfo();
        OnboardingData onboardingData = mockInstance(new OnboardingData());
        onboardingData.setAttributes(List.of(mockInstance(new Attribute())));
        onboardingData.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        onBoardingInfo.setInstitutions(singletonList(onboardingData));
        when(msCoreRestClientMock.getOnBoardingInfo(any(), any(), any()))
                .thenReturn(onBoardingInfo);
        // when
        InstitutionInfo institutionInfo = msCoreConnector.getOnBoardedInstitution(institutionId);
        // then
        assertNotNull(institutionInfo);
        checkNotNullFields(institutionInfo, "paymentServiceProvider", "dataProtectionOfficer");
        assertEquals(onboardingData.getDescription(), institutionInfo.getDescription());
        assertEquals(onboardingData.getDigitalAddress(), institutionInfo.getDigitalAddress());
        assertEquals(onboardingData.getExternalId(), institutionInfo.getExternalId());
        assertEquals(onboardingData.getState(), institutionInfo.getStatus());
        assertEquals(onboardingData.getAttributes().get(0).getDescription(), institutionInfo.getCategory());
        assertEquals(onboardingData.getGeographicTaxonomies().get(0).getCode(), institutionInfo.getGeographicTaxonomies().get(0).getCode());
        assertEquals(onboardingData.getGeographicTaxonomies().get(0).getDesc(), institutionInfo.getGeographicTaxonomies().get(0).getDesc());
        assertEquals(onboardingData.getZipCode(), institutionInfo.getZipCode());
        reflectionEqualsByName(onboardingData.getPaymentServiceProvider(), institutionInfo.getPaymentServiceProvider());
        reflectionEqualsByName(onboardingData.getSupportContact(), institutionInfo.getSupportContact());
        reflectionEqualsByName(onboardingData.getBilling(), institutionInfo.getBilling());
        verify(msCoreRestClientMock, times(1))
                .getOnBoardingInfo(institutionId, null, EnumSet.of(ACTIVE));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void getOnBoardedInstitutions_toBeValidatedtoBeValidated() {
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
    void updateGeographicTaxonomy() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomiesMock = new GeographicTaxonomyList();
        geographicTaxonomiesMock.setGeographicTaxonomyList(List.of(mockInstance(new GeographicTaxonomy())));
        System.out.println(geographicTaxonomiesMock);
        Mockito.doNothing()
                .when(msCoreRestClientMock).updateInstitutionGeographicTaxonomy(anyString(), any());
        // when
        msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomiesMock);
        // then
        ArgumentCaptor<InstitutionPut> argumentCaptor = ArgumentCaptor.forClass(InstitutionPut.class);
        verify(msCoreRestClientMock, times(1))
                .updateInstitutionGeographicTaxonomy(Mockito.eq(institutionId), argumentCaptor.capture());
        InstitutionPut institutionPut = argumentCaptor.getValue();
        assertEquals(geographicTaxonomiesMock.getGeographicTaxonomyList().get(0).getCode(), institutionPut.getGeographicTaxonomyCodes().get(0));
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void updateGeographicTaxonomy_hasNullInstitutionId() {
        // given
        String institutionId = null;
        GeographicTaxonomyList geographicTaxonomiesMock = new GeographicTaxonomyList();
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomiesMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_INSTITUTION_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void updateGeographicTaxonomy_hasNullGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        GeographicTaxonomyList geographicTaxonomiesMock = null;
        // when
        Executable executable = () -> msCoreConnector.updateInstitutionGeographicTaxonomy(institutionId, geographicTaxonomiesMock);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_GEOGRAPHIC_TAXONOMIES_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
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
        MsCoreConnectorImpl msCoreConnector = new MsCoreConnectorImpl(msCoreRestClientMock);

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
        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/relationInfo-to-userInfo.json");
        RelationshipInfo relationshipInfo = mapper.readValue(stub, RelationshipInfo.class);
        // when
        UserInfo userInfo = MsCoreConnectorImpl.RELATIONSHIP_INFO_TO_USER_INFO_FUNCTION.apply(relationshipInfo);
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

        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/multi-role.json");
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

        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/higher-role-active.json");
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


        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/merge.json");
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


        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/higher-role-pending.json");
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

        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/active-role-different-status.json");
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

        File stub = ResourceUtils.getFile("classpath:stubs/MsCoreConnectorImplTest/getUserInstitutionRelationships/active-role-different-status-2.json");
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
    void createUsers_nullInstitutionId() {
        // given
        String institutionId = null;
        String productId = "productId";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = new CreateUserDto();
        // when
        Executable executable = () -> {
            msCoreConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
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
            msCoreConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Product id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
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
            msCoreConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A User is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
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
            msCoreConnector.createUsers(institutionId, productId, userId, createUserDto);
        };
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An User Id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void createUsers(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto);
        // then
        switch (partyRole) {
            case SUB_DELEGATE:
                assertDoesNotThrow(executable);
                verify(msCoreRestClientMock, times(1))
                        .onboardingSubdelegates(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor, userId);
                break;
            case OPERATOR:
                assertDoesNotThrow(executable);
                verify(msCoreRestClientMock, times(1))
                        .onboardingOperators(onboardingRequestCaptor.capture());
                verifyRequest(institutionId, productId, createUserDto, onboardingRequestCaptor, userId);
                break;
            default:
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
                assertEquals("Invalid Party role", e.getMessage());
        }
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void checkExistingRelationshipRoles_userExistingConflict1(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(Optional.of(productId));
        mockUserInfoFilter.setUserId(Optional.ofNullable(userId));
        mockUserInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        RelationshipInfo mockRelationshipInfo = new RelationshipInfo();
        mockRelationshipInfo.setFrom("from");
        mockRelationshipInfo.setId("id");
        mockRelationshipInfo.setTo("to");
        it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo productInfo = new it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo();
        productInfo.setId("productId");
        productInfo.setRole("Operator Api");
        mockRelationshipInfo.setProduct(productInfo);
        RelationshipsResponse mockRelationshipsResponse = new RelationshipsResponse();
        mockRelationshipsResponse.add(mockRelationshipInfo);
        when(msCoreRestClientMock.getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockRelationshipsResponse);
        // when
        Executable executable = () -> msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("User role conflict", e.getMessage());
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString());
    }

    void checkExistingRelationshipRoles_userExistingConflict2(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(Optional.of(productId));
        mockUserInfoFilter.setUserId(Optional.ofNullable(userId));
        mockUserInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        RelationshipInfo mockRelationshipInfo = new RelationshipInfo();
        mockRelationshipInfo.setFrom("from");
        mockRelationshipInfo.setId("id");
        mockRelationshipInfo.setTo("to");
        it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo productInfo = new it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo();
        productInfo.setId("productId");
        productInfo.setRole("Operator security");
        mockRelationshipInfo.setProduct(productInfo);
        RelationshipsResponse mockRelationshipsResponse = new RelationshipsResponse();
        mockRelationshipsResponse.add(mockRelationshipInfo);
        when(msCoreRestClientMock.getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockRelationshipsResponse);
        // when
        Executable executable = () -> msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("User role conflict", e.getMessage());
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString());
    }

    @Test
    void checkExistingRelationshipRoles_userExistingNoConflict() {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(PartyRole.OPERATOR);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(Optional.of(productId));
        mockUserInfoFilter.setUserId(Optional.ofNullable(userId));
        mockUserInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        RelationshipInfo mockRelationshipInfo = new RelationshipInfo();
        mockRelationshipInfo.setFrom("from");
        mockRelationshipInfo.setId("id");
        mockRelationshipInfo.setTo("to");
        mockRelationshipInfo.setRole(PartyRole.OPERATOR);
        it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo productInfo = new it.pagopa.selfcare.dashboard.connector.rest.model.product.ProductInfo();
        productInfo.setId("productId");
        productInfo.setRole("Operator Api");
        mockRelationshipInfo.setProduct(productInfo);
        RelationshipsResponse mockRelationshipsResponse = new RelationshipsResponse();
        mockRelationshipsResponse.add(mockRelationshipInfo);
        when(msCoreRestClientMock.getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockRelationshipsResponse);
        // when
        msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId);
        // then
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString());
    }

    @ParameterizedTest
    @EnumSource(value = PartyRole.class)
    void checkExistingRelationshipRoles_noUserExisting(PartyRole partyRole) {
        // given
        String institutionId = "institutionId";
        String productId = "productId";
        String productRoles = "Operator Api";
        String userId = UUID.randomUUID().toString();
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock = mockInstance(new CreateUserDto.Role(), "setPartyRole");
        roleMock.setProductRole(productRoles);
        roleMock.setPartyRole(partyRole);
        createUserDto.setRoles(Set.of(roleMock));
        UserInfo.UserInfoFilter mockUserInfoFilter = new UserInfo.UserInfoFilter();
        mockUserInfoFilter.setProductId(Optional.of(productId));
        mockUserInfoFilter.setUserId(Optional.ofNullable(userId));
        mockUserInfoFilter.setAllowedState(Optional.of(EnumSet.of(ACTIVE)));
        RelationshipInfo mockRelationshipInfo = new RelationshipInfo();
        mockRelationshipInfo.setFrom("from");
        mockRelationshipInfo.setId("id");
        mockRelationshipInfo.setTo("to");
        RelationshipsResponse mockRelationshipsResponse = new RelationshipsResponse();
        when(msCoreRestClientMock.getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString()))
                .thenReturn(mockRelationshipsResponse);
        // when
        msCoreConnector.checkExistingRelationshipRoles(institutionId, productId, createUserDto, userId);
        // then
        verify(msCoreRestClientMock, times(1))
                .getUserInstitutionRelationships(anyString(), any(), any(), any(), any(), anyString());
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
        CreateUserDto createUserDto = mockInstance(new CreateUserDto(), "setRoles");
        CreateUserDto.Role roleMock1 = mockInstance(new CreateUserDto.Role(), "setPartyROle");
        CreateUserDto.Role roleMock2 = mockInstance(new CreateUserDto.Role(), "setPartyROle");

        roleMock1.setProductRole(productRoles1);
        roleMock1.setPartyRole(partyRole1);
        roleMock2.setProductRole(productRoles2);
        roleMock2.setPartyRole(partyRole2);
        createUserDto.setRoles(Set.of(roleMock1, roleMock2));
        // when
        Executable executable = () -> msCoreConnector.createUsers(institutionId, productId, userId, createUserDto);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Is not allowed to create both SUB_DELEGATE and OPERATOR users", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
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
        Executable executable = () -> msCoreConnector.suspend(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Relationship id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void suspend() {
        // given
        String relationshipId = "relationshipId";
        // when
        msCoreConnector.suspend(relationshipId);
        // then
        verify(msCoreRestClientMock, times(1))
                .suspendRelationship(relationshipId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void activate_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> msCoreConnector.activate(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Relationship id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void activate() {
        // given
        String relationshipId = "relationshipId";
        // when
        msCoreConnector.activate(relationshipId);
        // then
        verify(msCoreRestClientMock, times(1))
                .activateRelationship(relationshipId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void delete_nullRelationshipId() {
        // given
        String relationshipId = null;
        // when
        Executable executable = () -> msCoreConnector.delete(relationshipId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A Relationship id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void delete() {
        // given
        String relationshipId = "relationshipId";
        // when
        msCoreConnector.delete(relationshipId);
        // then
        verify(msCoreRestClientMock, times(1))
                .deleteRelationshipById(relationshipId);
        verifyNoMoreInteractions(msCoreRestClientMock);
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
    void getInstitutionByExternalId_nullInstitutionId() {
        // given
        String institutionExternalId = null;
        // when
        Executable executable = () -> msCoreConnector.getInstitutionByExternalId(institutionExternalId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution external id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitutionByExternalId_nullResponse() {
        // given
        String institutionExternalId = "institutionExternalId";
        // when
        Institution institution = msCoreConnector.getInstitutionByExternalId(institutionExternalId);
        // then
        assertNull(institution);
        verify(msCoreRestClientMock, times(1))
                .getInstitutionByExternalId(institutionExternalId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getInstitutionByExternalId() {
        // given
        String institutionExternalId = "institutionExternalId";
        Institution institutionMock = mockInstance(new Institution());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionMock.setAttributes(List.of(attribute));
        when(msCoreRestClientMock.getInstitutionByExternalId(any()))
                .thenReturn(institutionMock);
        // when
        Institution institution = msCoreConnector.getInstitutionByExternalId(institutionExternalId);
        // then
        assertSame(institutionMock, institution);
        checkNotNullFields(institution);
        verify(msCoreRestClientMock, times(1))
                .getInstitutionByExternalId(institutionExternalId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getOnboardingRequestInfo() {
        // given
        final TokenInfo tokenInfoMock = mockInstance(new TokenInfo(), "setId", "setLegals");
        tokenInfoMock.setId(UUID.randomUUID());
        final RelationshipBinding managerRelationshipBinding = mockInstance(new RelationshipBinding(), "setRole");
        managerRelationshipBinding.setRole(PartyRole.MANAGER);
        final RelationshipBinding adminRelationshipBinding = mockInstance(new RelationshipBinding(), "setRole");
        adminRelationshipBinding.setRole(PartyRole.DELEGATE);
        tokenInfoMock.setLegals(List.of(managerRelationshipBinding, adminRelationshipBinding));
        when(msCoreRestClientMock.getToken(any()))
                .thenReturn(tokenInfoMock);
        final Relationship managerRelationshipMock = mockInstance(new Relationship());
        when(msCoreRestClientMock.getRelationshipById(any()))
                .thenReturn(managerRelationshipMock);
        // when
        final OnboardingRequestInfo result = msCoreConnector.getOnboardingRequestInfo(tokenInfoMock.getId().toString());
        // then
        assertNotNull(result);
        assertNotNull(result.getInstitutionInfo());
        assertEquals(managerRelationshipMock.getTo().toString(), result.getInstitutionInfo().getId());
        assertEquals(managerRelationshipMock.getState(), result.getInstitutionInfo().getStatus());
        assertEquals(managerRelationshipMock.getBilling(), result.getInstitutionInfo().getBilling());
        assertNotNull(result.getManager());
        assertEquals(managerRelationshipBinding.getPartyId().toString(), result.getManager().getId());
        assertEquals(ADMIN, result.getManager().getRole());
        assertNotNull(result.getAdmins());
        assertEquals(1, result.getAdmins().size());
        assertEquals(adminRelationshipBinding.getPartyId().toString(), result.getAdmins().get(0).getId());
        assertEquals(ADMIN, result.getAdmins().get(0).getRole());
        verify(msCoreRestClientMock, times(1))
                .getToken(tokenInfoMock.getId());
        verify(msCoreRestClientMock, times(1))
                .getRelationshipById(managerRelationshipBinding.getRelationshipId());
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void relationshipToInstitutionInfoFunction() {
        // given
        Relationship relationshipMock = mockInstance(new Relationship());
        // when
        final InstitutionInfo result = MsCoreConnectorImpl.RELATIONSHIP_TO_INSTITUTION_INFO_FUNCTION.apply(relationshipMock);
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
    void getOnboardingRequestInfo_hasNullToken() {
        // given
        String tokenId = null;
        // when
        Executable executable = () -> msCoreConnector.getOnboardingRequestInfo(tokenId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_TOKEN_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void approveOnboardingRequest() {
        // given
        String tokenId = UUID.randomUUID().toString();
        Mockito.doNothing()
                .when(msCoreRestClientMock).approveOnboardingRequest(anyString());
        // when
        msCoreConnector.approveOnboardingRequest(tokenId);
        // then
        verify(msCoreRestClientMock, times(1))
                .approveOnboardingRequest(tokenId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void approveOnboardingRequest_hasNullToken() {
        // given
        String tokenId = null;
        // when
        Executable executable = () -> msCoreConnector.approveOnboardingRequest(tokenId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_TOKEN_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void rejectOnboardingRequest() {
        // given
        String tokenId = UUID.randomUUID().toString();
        Mockito.doNothing()
                .when(msCoreRestClientMock).rejectOnboardingRequest(anyString());
        // when
        msCoreConnector.rejectOnboardingRequest(tokenId);
        // then
        verify(msCoreRestClientMock, times(1))
                .rejectOnboardingRequest(tokenId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

    @Test
    void rejectOnboardingRequest_hasNullToken() {
        // given
        String tokenId = null;
        // when
        Executable executable = () -> msCoreConnector.rejectOnboardingRequest(tokenId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(REQUIRED_TOKEN_ID_MESSAGE, e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }

    @Test
    void getGeographicTaxonomyList_nullInstitutionId() {
        // given
        String institutionId = null;
        // when
        Executable executable = () -> msCoreConnector.getGeographicTaxonomyList(institutionId);
        // then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Institution id is required", e.getMessage());
        verifyNoInteractions(msCoreRestClientMock);
    }


    @Test
    void getGeographicTaxonomyList_noGeographicTaxonomies() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreRestClientMock.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        Executable executable = () -> msCoreConnector.getGeographicTaxonomyList(institutionId);
        // then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals(String.format("The institution %s does not have geographic taxonomies.", institutionId), e.getMessage());
        verify(msCoreRestClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }


    @Test
    void getGeographicTaxonomyList() {
        // given
        String institutionId = "institutionId";
        Institution institutionMock = mockInstance(new Institution());
        Attribute attribute = mockInstance(new Attribute());
        institutionMock.setGeographicTaxonomies(List.of(mockInstance(new GeographicTaxonomy())));
        institutionMock.setAttributes(List.of(attribute));
        when(msCoreRestClientMock.getInstitution(any()))
                .thenReturn(institutionMock);
        // when
        List<GeographicTaxonomy> geographicTaxonomies = msCoreConnector.getGeographicTaxonomyList(institutionId);
        // then
        assertSame(institutionMock.getGeographicTaxonomies(), geographicTaxonomies);
        assertNotNull(geographicTaxonomies);
        verify(msCoreRestClientMock, times(1))
                .getInstitution(institutionId);
        verifyNoMoreInteractions(msCoreRestClientMock);
    }

}