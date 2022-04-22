package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.UserDto;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserDetailsResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateUserDto;
import it.pagopa.selfcare.dashboard.web.model.product.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.user.UserResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        UserInfo model = TestUtils.mockInstance(new UserInfo());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        model.setProducts(productInfoMap);
        // when
        InstitutionUserResource resource = UserMapper.toInstitutionUser(model);
        // then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getSurname(), resource.getSurname());
        assertEquals(model.getEmail(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
        ProductInfo prodInfo = model.getProducts().get(productInfo.getId());
        assertEquals(productInfo.getId(), prodInfo.getId());
        assertEquals(productInfo.getTitle(), prodInfo.getTitle());
        TestUtils.reflectionEqualsByName(resource, model);
    }

    @Test
    void toInstitutionUserDetails() {
        // given
        UserInfo model = TestUtils.mockInstance(new UserInfo());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        model.setProducts(productInfoMap);
        // when
        InstitutionUserDetailsResource resource = UserMapper.toInstitutionUserDetails(model);
        // then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getSurname(), resource.getSurname());
        assertEquals(model.getEmail(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
        ProductInfo prodInfo = model.getProducts().get(productInfo.getId());
        assertEquals(productInfo.getId(), prodInfo.getId());
        assertEquals(productInfo.getTitle(), prodInfo.getTitle());
        assertEquals(model.getTaxCode(), resource.getFiscalCode());
        assertEquals(model.isCertified(), resource.isCertification());
        TestUtils.reflectionEqualsByName(resource, model);
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
        UserInfo model = TestUtils.mockInstance(new UserInfo());
        ProductInfo productMock = TestUtils.mockInstance(new ProductInfo());
        productMock.setRoleInfos(List.of(TestUtils.mockInstance(new RoleInfo())));
        Map<String, ProductInfo> product = new HashMap<>();
        product.put(productMock.getId(), productMock);
        model.setProducts(product);
        String id = model.getProducts().keySet().toArray()[0].toString();
        ProductInfo productInfo = model.getProducts().get(id);
        // when
        ProductUserResource resource = UserMapper.toProductUser(model);
        // then
        assertEquals(model.getId(), resource.getId());
        assertEquals(productInfo.getId(), resource.getProduct().getId());
        assertEquals(productInfo.getTitle(), resource.getProduct().getTitle());
        assertEquals(productInfo.getRoleInfos().get(0).getRole(), resource.getProduct().getRoleInfos().get(0).getRole());
        assertEquals(productInfo.getRoleInfos().get(0).getSelcRole(), resource.getProduct().getRoleInfos().get(0).getSelcRole());
        assertEquals(productInfo.getRoleInfos().get(0).getRelationshipId(), resource.getProduct().getRoleInfos().get(0).getRelationshipId());
        assertEquals(productInfo.getRoleInfos().get(0).getStatus(), resource.getProduct().getRoleInfos().get(0).getStatus());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getSurname(), resource.getSurname());
        assertEquals(model.getEmail(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
        TestUtils.reflectionEqualsByName(resource, model);
    }

    @Test
    void toUserResource_null() {
        // given
        String institutionId = "institutionId";
        it.pagopa.selfcare.dashboard.connector.model.user.UserResource model = null;
        // when
        UserResource resource = UserMapper.toUserResource(model, institutionId);
        // then
        assertNull(resource);
    }

    @Test
    void toUserResource_notNull() {
        //given
        String institutionId = "institutionId";
        it.pagopa.selfcare.dashboard.connector.model.user.UserResource model = TestUtils.mockInstance(new it.pagopa.selfcare.dashboard.connector.model.user.UserResource(), "setId");
        model.setId(UUID.randomUUID());
        //when
        UserResource resource = UserMapper.toUserResource(model, institutionId);
        //then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getEmail().getValue(), resource.getEmail().getValue());
        assertEquals(model.getName().getValue(), resource.getName().getValue());
        assertEquals(model.getFamilyName().getValue(), resource.getFamilyName().getValue());
    }

    @Test
    void fromCreateUserDto_null() {
        // given
        CreateUserDto dto = null;
        // when
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.fromCreateUserDto(dto);
        // then
        assertNull(model);
    }


    @Test
    void fromCreateUserDto_notNull() {
        // given
        CreateUserDto dto = TestUtils.mockInstance(new CreateUserDto());
        // when
        it.pagopa.selfcare.dashboard.connector.model.user.CreateUserDto model = UserMapper.fromCreateUserDto(dto);
        // then
        assertNotNull(model);
        TestUtils.reflectionEqualsByName(model, dto, "partyRole");
    }

    @Test
    void fromUpdateUserDto_null() {
        //given
        UpdateUserDto dto = null;
        String institutionId = "institutionId";
        //when
        UserDto model = UserMapper.fromUpdateUser(dto, institutionId);
        //then
        assertNull(model);
    }

    @Test
    void fromUpdateUserDto_notNull() {
        //given
        UpdateUserDto dto = TestUtils.mockInstance(new UpdateUserDto());
        String institutionId = "institutionId";
        //when
        UserDto model = UserMapper.fromUpdateUser(dto, institutionId);
        //then
        assertNotNull(model);
        assertNotNull(model.getWorkContacts());
        assertEquals(dto.getEmail(), model.getWorkContacts().get(institutionId).getEmail());
        assertEquals(dto.getEmail(), model.getEmail());
        assertEquals(dto.getName(), model.getName());
        assertEquals(dto.getSurname(), model.getFamilyName());
        TestUtils.reflectionEqualsByName(dto, model);
    }

    @Test
    void fromUpdateUserDto_nullInstitutionId() {
        //given
        UpdateUserDto dto = TestUtils.mockInstance(new UpdateUserDto());
        //when
        UserDto model = UserMapper.fromUpdateUser(dto, null);
        //then
        assertNull(model.getWorkContacts());
    }

}