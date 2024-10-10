package it.pagopa.selfcare.dashboard.core;

import com.fasterxml.jackson.core.type.TypeReference;
import it.pagopa.selfcare.dashboard.connector.api.MsCoreConnector;
import it.pagopa.selfcare.dashboard.connector.api.ProductsConnector;
import it.pagopa.selfcare.dashboard.connector.api.UserApiConnector;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionBase;
import it.pagopa.selfcare.dashboard.connector.model.institution.OnboardedProduct;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.core.exception.InvalidOnboardingStatusException;
import it.pagopa.selfcare.dashboard.core.exception.InvalidProductRoleException;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.product.entity.PHASE_ADDITION_ALLOWED;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRole;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UserV2ServiceImplTest extends BaseServiceTest {

    @InjectMocks
    private UserV2ServiceImpl userV2ServiceImpl;
    @Mock
    private MsCoreConnector msCoreConnectorMock;
    @Mock
    private UserGroupV2Service userGroupServiceMock;
    @Mock
    private UserApiConnector userApiConnectorMock;
    @Mock
    private ProductsConnector productsConnectorMock;

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void getInstitutionsNullUserId() {

        String userId = null;
        Collection<InstitutionBase> result = userV2ServiceImpl.getInstitutions(userId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getInstitutions() throws IOException {

        String userId = "userId";
        ClassPathResource resource = new ClassPathResource("expectations/InstitutionBase.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Collection<InstitutionBase> expectedInstitutions = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.getUserInstitutions(userId)).thenReturn((List<InstitutionBase>) expectedInstitutions);
        Collection<InstitutionBase> result = userV2ServiceImpl.getInstitutions(userId);
        Assertions.assertEquals(expectedInstitutions, result);
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUserInstitutions(userId);
    }

    @Test
    void getUserByIdNullUserId() {

        String userId = null;
        String institutionId = "institutionId";
        List<String> fields = new ArrayList<>();
        User result = userV2ServiceImpl.getUserById(userId, institutionId, fields);
        Assertions.assertNull(result);
    }

    @Test
    void getUserById() throws IOException {

        String userId = "userId";
        String institutionId = "institutionId";
        List<String> fields = new ArrayList<>();

        ClassPathResource resource = new ClassPathResource("expectations/User.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        User user = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.getUserById(userId, "institutionId", new ArrayList<>())).thenReturn(user);

        User result = userV2ServiceImpl.getUserById(userId, institutionId, fields);
        Assertions.assertEquals(user, result);
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUserById(userId, institutionId, fields);
    }

    @Test
    void searchUserByFiscalCodeNullFiscalCode() {

        String fiscalCode = null;
        String institutionId = "institutionId";
        User result = userV2ServiceImpl.searchUserByFiscalCode(fiscalCode, institutionId);
        Assertions.assertNull(result);
    }

    @Test
    void searchUserByFiscalCodeNullReturn() {

        String fiscalCode = "fiscalCode";
        String institutionId = "institutionId";
        when(userApiConnectorMock.searchByFiscalCode(fiscalCode, institutionId)).thenReturn(null);
        User result = userV2ServiceImpl.searchUserByFiscalCode(fiscalCode, institutionId);
        Assertions.assertNull(result);
    }

    @Test
    void searchUserByFiscalCode() throws IOException {

        String fiscalCode = "fiscalCode";
        String institutionId = "institutionId";
        ClassPathResource resource = new ClassPathResource("expectations/User.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        User user = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });
        when(userApiConnectorMock.searchByFiscalCode(fiscalCode, institutionId)).thenReturn(user);
        User result = userV2ServiceImpl.searchUserByFiscalCode(fiscalCode, institutionId);
        Assertions.assertEquals(user, result);
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).searchByFiscalCode(fiscalCode, institutionId);
    }

    @Test
    void getUsersByInstitutionIdWithoutInstitutionId() {

        String institutionId = null;
        String productId = "productId";
        List<String> productRoles = new ArrayList<>();
        String loggedUserId = "loggedUserId";

        Collection<UserInfo> result = userV2ServiceImpl.getUsersByInstitutionId(institutionId, productId, productRoles, loggedUserId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByInstitutionIdEmptyUserInfo() {

        String institutionId = "institutionId";
        String productId = "productId";
        String loggedUserId = "loggedUserId";
        List<String> productRoles = new ArrayList<>();
        productRoles.add("productRole");

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles);

        when(userApiConnectorMock.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(new ArrayList<>());

        Collection<UserInfo> result = userV2ServiceImpl.getUsersByInstitutionId(institutionId, productId, productRoles, loggedUserId);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByInstitutionId() throws IOException {

        String institutionId = "institutionId";
        String productId = "productId";
        List<String> productRoles = new ArrayList<>();
        productRoles.add("productRole");
        String loggedUserId = "loggedUserId";

        UserInfo.UserInfoFilter userInfoFilter = new UserInfo.UserInfoFilter();
        userInfoFilter.setProductId(productId);
        userInfoFilter.setProductRoles(productRoles);

        ClassPathResource resource = new ClassPathResource("expectations/CollectionUserInfo.json");
        byte[] resourceStream = Files.readAllBytes(resource.getFile().toPath());
        Collection<UserInfo> userInfo = objectMapper.readValue(resourceStream, new TypeReference<>() {
        });

        when(userApiConnectorMock.getUsers(institutionId, userInfoFilter, loggedUserId)).thenReturn(userInfo);

        Collection<UserInfo> result = userV2ServiceImpl.getUsersByInstitutionId(institutionId, productId, productRoles, loggedUserId);
        Assertions.assertEquals(userInfo, result);
        Mockito.verify(userApiConnectorMock, Mockito.times(1)).getUsers(institutionId, userInfoFilter, loggedUserId);
    }

    @Test
    void suspend() {

        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        userV2ServiceImpl.suspendUserProduct(userId, institutionid, productId, productRole);
        verify(userApiConnectorMock, times(1))
                .suspendUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userApiConnectorMock);
    }


    @Test
    void activate() {

        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        userV2ServiceImpl.activateUserProduct(userId, institutionid, productId, productRole);

        Mockito.verify(userApiConnectorMock, Mockito.times(1))
                .activateUserProduct(userId, institutionid, productId, productRole);
        Mockito.verifyNoMoreInteractions(userApiConnectorMock);
    }

    @Test
    void delete() {

        String userId = "rel1";
        String institutionid = "id1";
        String productId = "prod-pagopa";
        String productRole = "admin";

        userV2ServiceImpl.deleteUserProduct(userId, institutionid, productId, productRole);

        Mockito.verify(userApiConnectorMock, Mockito.times(1))
                .deleteUserProduct(userId, institutionid, productId, productRole);
        Mockito.verify(userGroupServiceMock, Mockito.times(1))
                .deleteMembersByUserId(userId, institutionid, productId);
        Mockito.verifyNoMoreInteractions(userApiConnectorMock, userGroupServiceMock);
    }

    @Test
    void updateUser() {

        final String institutionId = "institutionId";
        final String userId = "userId";
        final UpdateUserRequestDto user = mockInstance(new UpdateUserRequestDto());
        Institution institutionMock = mockInstance(new Institution());
        when(msCoreConnectorMock.getInstitution(Mockito.anyString()))
                .thenReturn(institutionMock);

        Executable executable = () -> userV2ServiceImpl.updateUser(userId, institutionId, user);

        assertDoesNotThrow(executable);
        verify(msCoreConnectorMock, times(1))
                .getInstitution(institutionId);
        verify(userApiConnectorMock, times(1))
                .updateUser(userId, institutionId, user);
        verifyNoMoreInteractions(userApiConnectorMock, msCoreConnectorMock);
    }

    @Test
    void updateUser_nullInstitution() {

        final String institutionId = "institutionId";
        final String userId = "userId";
        final UpdateUserRequestDto user = mockInstance(new UpdateUserRequestDto());

        Executable executable = () -> userV2ServiceImpl.updateUser(userId, institutionId, user);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, executable);
        assertEquals("There is no institution for given institutionId", exception.getMessage());
        verifyNoInteractions(userApiConnectorMock);
    }


    @Test
    void addUserProductRoles_ok() {

        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";
        Set<String> productRoles = new HashSet<>(List.of("operator"));
        String role = "MANAGER";
        Product product = getProduct();

        Institution institution = new Institution();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        institution.setOnboarding(List.of(onboardedProduct));

        CreateUserDto.Role roleDto = new CreateUserDto.Role();
        roleDto.setProductRole("operator");
        roleDto.setLabel("operator");
        roleDto.setPartyRole(PartyRole.MANAGER);

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        when(productsConnectorMock.getProduct(productId)).thenReturn(product);

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        when(productsConnectorMock.getProduct(productId)).thenReturn(product);
        doNothing().when(userApiConnectorMock).createOrUpdateUserByUserId(institution, productId, userId, List.of(roleDto));

        userV2ServiceImpl.addUserProductRoles(institutionId, productId, userId, productRoles, role);

        verify(userApiConnectorMock, times(1))
                .createOrUpdateUserByUserId(institution, productId, userId, List.of(roleDto));
        verifyNoMoreInteractions(userApiConnectorMock);
    }

    @Test
    void addUserProductRoles_ok_withoutPartyRole() {

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

        CreateUserDto.Role roleDto = new CreateUserDto.Role();
        roleDto.setProductRole("operator");
        roleDto.setLabel("operator");
        roleDto.setPartyRole(PartyRole.OPERATOR);

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);
        when(productsConnectorMock.getProduct(productId)).thenReturn(product);
        doNothing().when(userApiConnectorMock).createOrUpdateUserByUserId(institution, productId, userId, List.of(roleDto));

        userV2ServiceImpl.addUserProductRoles(institutionId, productId, userId, productRoles, null);

        verify(userApiConnectorMock, times(1))
                .createOrUpdateUserByUserId(institution, productId, userId, List.of(roleDto));
        verifyNoMoreInteractions(userApiConnectorMock);
    }


    @Test
    void addUserProductRoles_invalidOnboardingStatus() {

        final String institutionId = "institutionId";
        final String productId = "productId";
        final String userId = "userId";
        Set<String> productRoles = new HashSet<>(List.of("operator"));
        String role = "MANAGER";

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(new Institution());

        Assertions.assertThrows(InvalidOnboardingStatusException.class, () -> userV2ServiceImpl.addUserProductRoles(institutionId, productId, userId, productRoles, role));

        verifyNoInteractions(userApiConnectorMock);
    }

    @Test
    void createUsersByFiscalCode() {

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


        CreateUserDto.Role roleDto = new CreateUserDto.Role();
        roleDto.setProductRole("operator");
        roleDto.setLabel("operator");
        roleDto.setPartyRole(PartyRole.OPERATOR);


        when(productsConnectorMock.getProduct(productId)).thenReturn(product);

        when(userApiConnectorMock.createOrUpdateUserByFiscalCode(institution, productId, userToCreate, List.of(roleDto))).thenReturn("userId");

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        String userId = userV2ServiceImpl.createUsers(institutionId, productId, userToCreate);

        assertNotNull(userId);
        ArgumentCaptor<List<CreateUserDto.Role>> captorRoles = ArgumentCaptor.forClass(List.class);
        verify(userApiConnectorMock, times(1))
                .createOrUpdateUserByFiscalCode(eq(institution), eq(productId), eq(userToCreate), captorRoles.capture());
        assertEquals(captorRoles.getValue().get(0).getProductRole(), productRole);
        assertEquals(captorRoles.getValue().get(0).getPartyRole(), roleDto.getPartyRole());
        verifyNoMoreInteractions(userApiConnectorMock);
    }

    @Test
    void createUsersByFiscalCodeWithPartyROle() {

        final String institutionId = "institutionId";
        final String productId = "productId";
        final String productRole = "operator";
        UserToCreate userToCreate = new UserToCreate();
        HashSet<String> productRoles = new HashSet<>();
        productRoles.add(productRole);
        userToCreate.setRole(PartyRole.MANAGER);
        userToCreate.setProductRoles(productRoles);

        Product product = getProduct();

        Institution institution = new Institution();
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId(productId);
        onboardedProduct.setStatus(RelationshipState.ACTIVE);
        institution.setOnboarding(List.of(onboardedProduct));

        CreateUserDto.Role roleDto = new CreateUserDto.Role();
        roleDto.setProductRole("operator");
        roleDto.setLabel("operator");
        roleDto.setPartyRole(PartyRole.MANAGER);

        when(productsConnectorMock.getProduct(productId)).thenReturn(product);

        when(userApiConnectorMock.createOrUpdateUserByFiscalCode(institution, productId, userToCreate, List.of(roleDto))).thenReturn("userId");

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(institution);

        String userId = userV2ServiceImpl.createUsers(institutionId, productId, userToCreate);

        assertNotNull(userId);
        ArgumentCaptor<List<CreateUserDto.Role>> captorRoles = ArgumentCaptor.forClass(List.class);
        verify(userApiConnectorMock, times(1))
                .createOrUpdateUserByFiscalCode(eq(institution), eq(productId), eq(userToCreate), captorRoles.capture());
        assertEquals(captorRoles.getValue().get(0).getProductRole(), productRole);
        assertEquals(captorRoles.getValue().get(0).getPartyRole(), roleDto.getPartyRole());
        verifyNoMoreInteractions(userApiConnectorMock);
    }


    @Test
    void createUsersByFiscalCodeWithOnboardingNotActive() {

        final String institutionId = "institutionId";
        final String productId = "productId";
        UserToCreate userToCreate = new UserToCreate();
        HashSet<String> productRoles = new HashSet<>();
        productRoles.add("operator");
        userToCreate.setProductRoles(productRoles);

        when(msCoreConnectorMock.getInstitution(institutionId)).thenReturn(new Institution());

        assertThrows(InvalidOnboardingStatusException.class, () -> userV2ServiceImpl.createUsers(institutionId, productId, userToCreate));

        verify(msCoreConnectorMock, times(1)).getInstitution(institutionId);
        verifyNoMoreInteractions(userApiConnectorMock);
    }

    private static Product getProduct() {
        Product product = new Product();
        Map<PartyRole, it.pagopa.selfcare.product.entity.ProductRoleInfo> map = new EnumMap<>(PartyRole.class);

        ProductRoleInfo productRoleInfoOperator = new ProductRoleInfo();
        productRoleInfoOperator.setPhasesAdditionAllowed(List.of(PHASE_ADDITION_ALLOWED.DASHBOARD.value));
        ProductRole productRole = new ProductRole();
        productRole.setCode("operator");
        productRole.setLabel("operator");
        productRoleInfoOperator.setRoles(List.of(productRole));
        map.put(PartyRole.OPERATOR, productRoleInfoOperator);

        ProductRoleInfo productRoleInfoDelegate= new ProductRoleInfo();
        productRoleInfoDelegate.setPhasesAdditionAllowed(List.of(PHASE_ADDITION_ALLOWED.DASHBOARD.value));
        ProductRole productRole2 = new ProductRole();
        productRole2.setCode("delegate");
        productRole2.setLabel("delegate");
        productRoleInfoDelegate.setRoles(List.of(productRole2));
        map.put(PartyRole.DELEGATE, productRoleInfoDelegate);


        product.setRoleMappings(map);
        return product;
    }
}
