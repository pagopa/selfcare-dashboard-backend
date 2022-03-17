package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.*;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        assertEquals(model.getTaxCode(), resource.getFiscalCode());
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
        User model = null;
        // when
        UserResource resource = UserMapper.toUserResource(model);
        // then
        assertNull(resource);
    }

    @Test
    void toUserResource_notNull() {
        //given
        User model = TestUtils.mockInstance(new User());
        //when
        UserResource resource = UserMapper.toUserResource(model);
        //then
        assertEquals(model.getEmail(), resource.getEmail());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getSurname(), resource.getSurname());
        assertEquals(model.isCertification(), resource.isCertification());
        TestUtils.reflectionEqualsByName(resource, model);
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
        //when
        UserDto model = UserMapper.fromUpdateUser(dto);
        //then
        assertNull(model);
    }

    @Test
    void fromUpdateUserDto_notNull() {
        //given
        UpdateUserDto dto = TestUtils.mockInstance(new UpdateUserDto());
        //when
        UserDto model = UserMapper.fromUpdateUser(dto);
        //then
        assertNotNull(model);
        TestUtils.reflectionEqualsByName(dto, model);
    }

}