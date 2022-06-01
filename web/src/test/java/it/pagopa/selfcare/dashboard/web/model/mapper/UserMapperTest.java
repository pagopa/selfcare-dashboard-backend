package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserProductRoles;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static it.pagopa.selfcare.commons.utils.TestUtils.*;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toInstitutionUser_null() {
        // given
        UserInfo model = null;
        // when
        InstitutionUserResource resource = UserMapper.toInstitutionUser(model);
        // then
        assertNull(resource);
    }


    @Test
    void toInstitutionUser_notNull() {
        // given
        UserInfo model = mockInstance(new UserInfo(), "setId");
        model.setId(randomUUID().toString());
        ProductInfo productInfo = mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        model.setProducts(productInfoMap);
        // when
        InstitutionUserResource resource = UserMapper.toInstitutionUser(model);
        // then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getUser().getName().getValue(), resource.getName());
        assertEquals(model.getUser().getFamilyName().getValue(), resource.getSurname());
        assertEquals(model.getUser().getEmail().getValue(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
        ProductInfo prodInfo = model.getProducts().get(productInfo.getId());
        assertEquals(productInfo.getId(), prodInfo.getId());
        assertEquals(productInfo.getTitle(), prodInfo.getTitle());
    }

    @Test
    void toInstitutionUserDetails() {
        // given
        UserInfo model = mockInstance(new UserInfo(), "setIndo");
        model.setId(randomUUID().toString());
        ProductInfo productInfo = mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        model.setProducts(productInfoMap);
        // when
        InstitutionUserDetailsResource resource = UserMapper.toInstitutionUserDetails(model);
        // then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getUser().getName().getValue(), resource.getName());
        assertEquals(model.getUser().getFamilyName().getValue(), resource.getSurname());
        assertEquals(model.getUser().getEmail().getValue(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
        ProductInfo prodInfo = model.getProducts().get(productInfo.getId());
        assertEquals(productInfo.getId(), prodInfo.getId());
        assertEquals(productInfo.getTitle(), prodInfo.getTitle());
        assertEquals(model.getUser().getFiscalCode(), resource.getFiscalCode());
    }

    @Test
    void toInstitutionUserDetail_null() {
        //given
        UserInfo model = null;
        //when
        InstitutionUserDetailsResource resource = UserMapper.toInstitutionUserDetails(model);
        //then
        assertNull(resource);
    }

    @Test
    void toProductUser_null() {
        // given
        UserInfo model = null;
        // when
        ProductUserResource resource = UserMapper.toProductUser(model);
        // then
        assertNull(resource);
    }


    @Test
    void toProductUser_notNull() {
        // given
        UserInfo model = mockInstance(new UserInfo());
        model.setId(randomUUID().toString());
        ProductInfo productMock = mockInstance(new ProductInfo());
        productMock.setRoleInfos(List.of(mockInstance(new RoleInfo())));
        Map<String, ProductInfo> product = new HashMap<>();
        product.put(productMock.getId(), productMock);
        model.setProducts(product);
        model.getUser().setWorkContacts(Map.of(model.getInstitutionId(), mockInstance(new WorkContact())));
        String id = model.getProducts().keySet().toArray()[0].toString();
        ProductInfo productInfo = model.getProducts().get(id);
        // when
        ProductUserResource resource = UserMapper.toProductUser(model);
        // then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(productInfo.getId(), resource.getProduct().getId());
        assertEquals(productInfo.getTitle(), resource.getProduct().getTitle());
        assertEquals(productInfo.getRoleInfos().get(0).getRole(), resource.getProduct().getRoleInfos().get(0).getRole());
        assertEquals(productInfo.getRoleInfos().get(0).getSelcRole(), resource.getProduct().getRoleInfos().get(0).getSelcRole());
        assertEquals(productInfo.getRoleInfos().get(0).getRelationshipId(), resource.getProduct().getRoleInfos().get(0).getRelationshipId());
        assertEquals(productInfo.getRoleInfos().get(0).getStatus(), resource.getProduct().getRoleInfos().get(0).getStatus());
        assertEquals(model.getUser().getName().getValue(), resource.getName());
        assertEquals(model.getUser().getFamilyName().getValue(), resource.getSurname());
        assertEquals(model.getUser().getWorkContacts().get(model.getInstitutionId()).getEmail().getValue(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
    }

    @Test
    void toUserResource_null() {
        // given
        String institutionId = "institutionId";
        User model = null;
        // when
        UserResource resource = UserMapper.toUserResource(model, institutionId);
        // then
        assertNull(resource);
    }

    @Test
    void toUserResource_notNull() {
        //given
        String institutionId = "institutionId";
        User model = mockInstance(new User(), "setId");
        model.setId(randomUUID().toString());
        Map<String, WorkContact> workContacts = new HashMap<>();
        WorkContact workcontact = mockInstance(new WorkContact());
        workContacts.put(institutionId, workcontact);
        model.setWorkContacts(workContacts);
        //when
        UserResource resource = UserMapper.toUserResource(model, institutionId);
        //then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getEmail().getValue(), resource.getEmail().getValue());
        assertEquals(model.getName().getValue(), resource.getName().getValue());
        assertEquals(model.getWorkContacts().get(institutionId).getEmail().getValue(), resource.getEmail().getValue());
        assertEquals(model.getFamilyName().getValue(), resource.getFamilyName().getValue());
    }

    @Test
    void toResource_nullWorkContact() {
        //given
        String institutionId = "institutionId";
        User model = mockInstance(new User(), "setId");
        model.setId(randomUUID().toString());
        //when
        UserResource resource = UserMapper.toUserResource(model, institutionId);
        //then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getName().getValue(), resource.getName().getValue());
        assertNull(resource.getEmail());
        assertEquals(model.getFamilyName().getValue(), resource.getFamilyName().getValue());
    }

    @Test
    void toResource_differentInstitutionId() {
        //given
        String institutionId = "institutionId";
        User model = mockInstance(new User(), "setId");
        model.setId(randomUUID().toString());
        Map<String, WorkContact> workContacts = new HashMap<>();
        WorkContact workcontact = mockInstance(new WorkContact());
        workContacts.put("institution2", workcontact);
        model.setWorkContacts(workContacts);
        //when
        UserResource resource = UserMapper.toUserResource(model, institutionId);
        //then
        assertEquals(model.getId(), resource.getId().toString());
        assertEquals(model.getName().getValue(), resource.getName().getValue());
        assertNull(resource.getEmail());
        assertEquals(model.getFamilyName().getValue(), resource.getFamilyName().getValue());
    }

    @Test
    void fromCreateUserDto_null() {
        // given
        CreateUserDto dto = null;
        final String institutionId = "institutionId";
        // when
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.fromCreateUserDto(dto, institutionId);
        // then
        assertNull(model);
    }


    @Test
    void fromCreateUserDto_notNull() {
        // given
        CreateUserDto dto = mockInstance(new CreateUserDto());
        final String institutionId = "institutionId";
        // when
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.fromCreateUserDto(dto, institutionId);
        // then
        assertNotNull(model);
        reflectionEqualsByName(model, dto, "partyRole", "name", "surname", "taxCode", "email");//TODO: fix excluded fields after Party API changes
        assertNotNull(model.getUser());
        assertEquals(dto.getTaxCode(), model.getUser().getFiscalCode());
        assertEquals(dto.getName(), model.getUser().getName().getValue());
        assertEquals(dto.getSurname(), model.getUser().getFamilyName().getValue());
        assertEquals(dto.getEmail(), model.getUser().getWorkContacts().get(institutionId).getEmail().getValue());
    }

    @Test
    void fromUpdateUserDto_null() {
        //given
        UpdateUserDto dto = null;
        String institutionId = "institutionId";
        //when
        MutableUserFieldsDto model = UserMapper.fromUpdateUser(dto, institutionId);
        //then
        assertNull(model);
    }

    @Test
    void toCreateUserDto_notNull() {
        //given
        UserProductRoles productRoles = mockInstance(new UserProductRoles());
        productRoles.setProductRoles(Set.of("productRoles"));
        //then
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.toCreateUserDto(productRoles);
        //then
        assertNotNull(model);
        checkNotNullFields(model, "user");
        model.getRoles().forEach(role -> checkNotNullFields(role, "label", "partyRole"));
    }

    @Test
    void toCreateUserDto_null() {
        //given
        UserProductRoles productRoles = null;
        //when
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.toCreateUserDto(productRoles);
        //then
        assertNull(model);
    }

    @Test
    void toCreateUserDto_nullRoles() {
        //given
        UserProductRoles productRoles = new UserProductRoles();
        //when
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.toCreateUserDto(productRoles);
        //then
        assertNotNull(model);
        assertNull(model.getRoles());
    }

    @Test
    void fromUpdateUserDto_notNull() {
        //given
        UpdateUserDto dto = mockInstance(new UpdateUserDto());
        String institutionId = "institutionId";
        //when
        MutableUserFieldsDto model = UserMapper.fromUpdateUser(dto, institutionId);
        //then
        assertNotNull(model);
        assertNotNull(model.getWorkContacts());
        assertNull(model.getEmail());
        assertEquals(1, model.getWorkContacts().size());
        assertTrue(model.getWorkContacts().containsKey(institutionId));
        assertCertifiedEquals(dto.getEmail(), model.getWorkContacts().get(institutionId).getEmail());
        assertCertifiedEquals(dto.getName(), model.getName());
        assertCertifiedEquals(dto.getSurname(), model.getFamilyName());
    }

    private static void assertCertifiedEquals(String expected, CertifiedField<String> actual) {
        assertEquals(expected, actual.getValue());
        assertEquals(Certification.NONE, actual.getCertification());
    }

    @Test
    void fromUpdateUserDto_nullInstitutionId() {
        //given
        UpdateUserDto dto = mockInstance(new UpdateUserDto());
        //when
        MutableUserFieldsDto model = UserMapper.fromUpdateUser(dto, null);
        //then
        assertNull(model.getWorkContacts());
    }

}