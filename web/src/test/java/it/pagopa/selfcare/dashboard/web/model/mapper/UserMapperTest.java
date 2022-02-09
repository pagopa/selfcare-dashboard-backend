package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.web.model.CreateUserDto;
import it.pagopa.selfcare.dashboard.web.model.InstitutionUserResource;
import it.pagopa.selfcare.dashboard.web.model.ProductUserResource;
import it.pagopa.selfcare.dashboard.web.model.UserResource;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        model.setProducts(List.of(productInfo));
        // when
        InstitutionUserResource resource = UserMapper.toInstitutionUser(model);
        // then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getSurname(), resource.getSurname());
        assertEquals(model.getEmail(), resource.getEmail());
        assertEquals(model.getRole(), resource.getRole());
        assertEquals(model.getStatus(), resource.getStatus());
        ProductInfo prodInfo = model.getProducts().iterator().next();
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
        // when
        ProductUserResource resource = UserMapper.toProductUser(model);
        // then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getRelationshipId(), resource.getRelationshipId());
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
        assertNull(model.getPartyRole());
    }

}