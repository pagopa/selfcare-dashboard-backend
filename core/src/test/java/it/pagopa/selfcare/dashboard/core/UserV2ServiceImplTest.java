package it.pagopa.selfcare.dashboard.core;

import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.institution.OnboardedProduct;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.exception.InvalidOnboardingStatusException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class UserV2ServiceImplTest {

    @Mock
    private UserRegistryConnector userConnectorMock;

    @InjectMocks
    private UserV2ServiceImpl userService;

    @Mock
    private MsCoreConnector msCoreConnectorMock;

    @Mock
    private UserApiConnector userApiConnector;

    @Mock
    private UserGroupV2Service userGroupService;

    @Mock
    private ProductsConnector productsConnector;

    @Test
    void getInstitutions() {
        // given
        String userId = "userId";
        InstitutionBase expectedInstitutionInfo = new InstitutionBase();

        when(userApiConnector.getUserInstitutions(userId)).thenReturn(List.of(expectedInstitutionInfo));
        // when
        Collection<InstitutionBase> institutions = userService.getInstitutions(userId);

        assertNotNull(institutions);
        assertEquals(1, institutions.size());
        assertSame(expectedInstitutionInfo, institutions.iterator().next());
        verifyNoMoreInteractions(msCoreConnectorMock);
    }


    @Test
    void suspend() {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        userService.suspendUserProduct(userId, institutionid, productId, productRole);
        Mockito.verify(userApiConnector, Mockito.times(1))
                .suspendUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userApiConnector);
    }


    @Test
    void activate() {
        // given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        // when
        userService.activateUserProduct(userId, institutionid, productId, productRole);
        // then
        Mockito.verify(userApiConnector, Mockito.times(1))
                .activateUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void delete() {
        //given
        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        //when
        userService.deleteUserProduct(userId, institutionid, productId, productRole);
        //then
        Mockito.verify(userApiConnector, Mockito.times(1))
                .deleteUserProduct(userId, institutionid, productId, productRole);
        Mockito.verify(userGroupService, Mockito.times(1))
                .deleteMembersByUserId(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnector, userGroupService);
    }

    @Test
    void getById(){
        //given
        final String userId = "userId";
        final String institutionId = "institutionId";
        final List<String> fields = List.of("fields");
        User user = mockInstance(new User());
        when(userApiConnector.getUserById(anyString(), anyString(), any())).thenReturn(user);
        //when
        User result = userService.getUserById(userId, institutionId, fields);
        //then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userApiConnector, times(1)).getUserById(userId, institutionId, fields);
    }

    @Test
    void searchByFiscalCode(){
        final String fiscalCode = "fiscalCode";
        final String institutionId = "institutionId";
        User user = mockInstance(new User());
        when(userApiConnector.searchByFiscalCode(anyString(), anyString())).thenReturn(user);
        //when
        User result = userService.searchUserByFiscalCode(fiscalCode, institutionId);
        //then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userApiConnector, times(1)).searchByFiscalCode(fiscalCode, institutionId);
    }

    @Test
    void updateUser() {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";
        final UpdateUserRequestDto user = mockInstance(new UpdateUserRequestDto());
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);
        //when
        Executable executable = () -> userService.updateUser(userId, institutionId, user);
        //then
        assertDoesNotThrow(executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(userApiConnector, times(1))
                .updateUser(userId, institutionId, user);
        verifyNoMoreInteractions(userConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullInstitution() {
        //given
        final String institutionId = "institutionId";
        final String userId = "userId";
        final UpdateUserRequestDto user = mockInstance(new UpdateUserRequestDto());
        //when
        Executable executable = () -> userService.updateUser(userId, institutionId, user);
        //then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There is no institution for given institutionId", exception.getMessage());
        verifyNoInteractions(userApiConnector);
    }

    @Test
    void getUsersByInstitutionId_returnsExpectedUsers() {
        // given
        final String institutionId = "inst1";
        final String productId = "prod1";
        final String loggedUserId = "loggedUserId";
        List<String> productRoles = List.of("productRole");
        UserInfo expectedUser = new UserInfo();
        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles);

        when(userApiConnector.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(List.of(expectedUser));
        // when
        Collection<UserInfo> users = userService.getUsersByInstitutionId(institutionId, productId,productRoles, loggedUserId);

        // then
        assertNotNull(users);
        assertEquals(1, users.size());
        assertSame(expectedUser, users.iterator().next());
        verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void getUsersByInstitutionId_emptyList() {
        // given
        final String institutionId = "inst1";
        final String productId = "prod1";
        final String loggedUserId = "loggedUserId";
        final List<String> productRoles = List.of("productRole");

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles);

        when(userApiConnector.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(Collections.emptyList());
        // when
        Collection<UserInfo> users = userService.getUsersByInstitutionId(institutionId, productId, productRoles, loggedUserId);

        // then
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void addUserProductRoles_ok() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";
        Set<String> productRoles = new HashSet<>(List.of("operator"));
        Product product = getProduct();

        Institution institution = new Institution();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        institution.setOnboarding(List.of(onboardedProduct));

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        when(productsConnector.getProduct(productId)).thenReturn(product);
        doNothing().when(userApiConnector).createOrUpdateUserByUserId(eq(institution), eq(productId), eq(userId), anyList());

        // when
        userService.addUserProductRoles(institutionId, productId, userId, productRoles);

        // then
        verify(userApiConnector, times(1))
                .createOrUpdateUserByUserId(eq(institution), eq(productId), eq(userId), anyList());
        verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void addUserProductRoles_invalidProductRole() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";
        Set<String> productRoles = new HashSet<>(List.of("role1"));
        Product product = getProduct();

        Institution institution = new Institution();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        institution.setOnboarding(List.of(onboardedProduct));

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        when(productsConnector.getProduct(productId)).thenReturn(product);

        // when
        Assertions.assertThrows(InvalidProductRoleException.class, () -> userService.addUserProductRoles(institutionId, productId, userId, productRoles));

        verifyNoInteractions(userApiConnector);
    }

    @Test
    void addUserProductRoles_invalidOnboardingStatus() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";
        Set<String> productRoles = new HashSet<>(List.of("operator"));

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(new Institution());

        // when
        Assertions.assertThrows(InvalidOnboardingStatusException.class, () -> userService.addUserProductRoles(institutionId, productId, userId, productRoles));

        verifyNoInteractions(userApiConnector);
    }

    @Test
    void createUsersByFiscalCode() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        final String productRole = "operator";
        UserToCreate userToCreate = new UserToCreate();
        HashSet<String> productRoles = new HashSet<>();
        productRoles.add(productRole);
        userToCreate.setProductRoles(productRoles);

        Product product = getProduct();

        Institution institution = new Institution();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        institution.setOnboarding(List.of(onboardedProduct));

        when(productsConnector.getProduct(productId)).thenReturn(product);

        when(userApiConnector.createOrUpdateUserByFiscalCode(eq(institution), eq(productId), eq(userToCreate), anyList())).thenReturn("userId");

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        // when
        String userId = userService.createUsers(institutionId, productId, userToCreate);

        // then
        assertNotNull(userId);
        ArgumentCaptor<List<CreateUserDto.Role>> captorRoles = ArgumentCaptor.forClass(List.class);
        verify(userApiConnector, times(1))
                .createOrUpdateUserByFiscalCode(eq(institution), eq(productId), eq(userToCreate), captorRoles.capture());
        assertEquals(captorRoles.getValue().get(0).getProductRole(), productRole);

        verifyNoMoreInteractions(userApiConnector);
    }

    @Test
    void createUsersByFiscalCodeWithOnboardingNotActive() {
        // given
        final String institutionId = "institutionId";
        final String productId = "productId";
        UserToCreate userToCreate = new UserToCreate();
        HashSet<String> productRoles = new HashSet<>();
        productRoles.add("operator");
        userToCreate.setProductRoles(productRoles);

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(new Institution());

        // when
        assertThrows(InvalidOnboardingStatusException.class, () -> userService.createUsers(institutionId, productId, userToCreate));

        // then
        verify(msCoreConnectorMock, times(1)).getInstitution(institutionId);
        verifyNoMoreInteractions(userApiConnector);
    }

    private static Product getProduct() {
        Product product = new Product();
        Map<PartyRole, it.pagopa.selfcare.product.entity.ProductRoleInfo> map = new EnumMap<>(PartyRole.class);
        ProductRoleInfo productRoleInfo = new ProductRoleInfo();
        ProductRole productRole = new ProductRole();
        productRole.setCode("operator");
        productRole.setLabel("operator");
        productRoleInfo.setRoles(List.of(productRole));
        map.put(PartyRole.OPERATOR, productRoleInfo);
        product.setRoleMappings(map);
        return product;
    }
}