package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UpdateUserGroup;
import it.pagopa.selfcare.dashboard.connector.model.groups.UserGroupInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.ProductInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.RoleInfo;
import it.pagopa.selfcare.dashboard.connector.model.user.User;
import it.pagopa.selfcare.dashboard.connector.model.user.UserInfo;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UpdateUserGroupDto;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupPlainResource;
import it.pagopa.selfcare.dashboard.web.model.user_groups.UserGroupResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    @Test
    void fromDtoCreateUserGroup_null() {
        //given
        CreateUserGroupDto dto = null;
        //when
        CreateUserGroup model = GroupMapper.fromDto(dto);
        //then
        assertNull(model);
    }

    @Test
    void fromDtoCreateUserGroup() {
        //given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        UUID id4 = UUID.randomUUID();
        Set<UUID> userIds = Set.of(id1, id2, id3, id4);
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        dto.setMembers(userIds);
        //when
        CreateUserGroup model = GroupMapper.fromDto(dto);
        //then
        assertNotNull(model);
        TestUtils.reflectionEqualsByName(dto, model);
    }

    @Test
    void fromDtoCreateUserGroup_nullMembersList() {
        //given
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        //when
        Executable executable = () -> GroupMapper.fromDto(dto);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Members list must not be null", e.getMessage());
    }

    @Test
    void fromDtoUpdateUserGroup_null() {
        //given
        UpdateUserGroupDto dto = null;
        //when
        UpdateUserGroup model = GroupMapper.fromDto(dto);
        //then
        assertNull(model);
    }

    @Test
    void fromDtoUpdateUserGroup() {
        //given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();
        UUID id4 = UUID.randomUUID();
        Set<UUID> userIds = Set.of(id1, id2, id3, id4);
        UpdateUserGroupDto dto = TestUtils.mockInstance(new UpdateUserGroupDto());
        dto.setMembers(userIds);
        //when
        UpdateUserGroup model = GroupMapper.fromDto(dto);
        //then
        assertNotNull(model);
        TestUtils.reflectionEqualsByName(dto, model);
    }

    @Test
    void fromDtoUpdateUserGroup_nullMembersList() {
        //given
        UpdateUserGroupDto dto = TestUtils.mockInstance(new UpdateUserGroupDto());
        //when
        Executable executable = () -> GroupMapper.fromDto(dto);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Members list must not be null", e.getMessage());
    }

    @Test
    void toResource_null() {
        //given
        UserGroupInfo model = null;
        //when
        UserGroupResource resource = GroupMapper.toResource(model);
        //then
        assertNull(resource);
    }

    @Test
    void toResource() {
        //given
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        userInfoModel.setId(UUID.randomUUID().toString());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = TestUtils.mockInstance(new User());
        model.setCreatedBy(userModel);
        model.setModifiedBy(userModel);
        Instant now = Instant.now();
        model.setModifiedAt(now);
        model.setCreatedAt(now);
        //when
        UserGroupResource resource = GroupMapper.toResource(model);
        //then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getDescription(), resource.getDescription());
        assertEquals(model.getStatus(), resource.getStatus());
        assertEquals(model.getInstitutionId(), resource.getInstitutionId());
        assertEquals(model.getProductId(), resource.getProductId());
        assertEquals(model.getStatus(), resource.getStatus());
        assertEquals(model.getCreatedAt(), resource.getCreatedAt());
        assertEquals(model.getCreatedBy().getName().getValue(), resource.getCreatedBy().getName());
        assertEquals(model.getCreatedBy().getFamilyName().getValue(), resource.getCreatedBy().getSurname());
        assertEquals(model.getCreatedBy().getId(), resource.getCreatedBy().getId());
        assertEquals(model.getModifiedAt(), resource.getModifiedAt());
        assertEquals(model.getModifiedBy().getId(), resource.getModifiedBy().getId());
        assertEquals(model.getModifiedBy().getName().getValue(), resource.getModifiedBy().getName());
        assertEquals(model.getModifiedBy().getFamilyName().getValue(), resource.getModifiedBy().getSurname());
    }

    @Test
    void toResource_productInfo() {
        //given
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        userInfoModel.setId(UUID.randomUUID().toString());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = TestUtils.mockInstance(new User());
        model.setCreatedBy(userModel);
        model.setModifiedBy(userModel);
        Instant now = Instant.now();
        model.setModifiedAt(now);
        model.setCreatedAt(now);
        //when
        UserGroupResource resource = GroupMapper.toResource(model);
        //then
        assertEquals(productInfo.getTitle(), resource.getMembers().get(0).getProduct().getTitle());
        assertEquals(productInfo.getId(), resource.getMembers().get(0).getProduct().getId());
        assertEquals(productInfo.getRoleInfos().get(0).getRole(), resource.getMembers().get(0).getProduct().getRoleInfos().get(0).getRole());
        assertEquals(productInfo.getRoleInfos().get(0).getSelcRole(), resource.getMembers().get(0).getProduct().getRoleInfos().get(0).getSelcRole());
        assertEquals(productInfo.getRoleInfos().get(0).getRelationshipId(), resource.getMembers().get(0).getProduct().getRoleInfos().get(0).getRelationshipId());
        assertEquals(productInfo.getRoleInfos().get(0).getStatus(), resource.getMembers().get(0).getProduct().getRoleInfos().get(0).getStatus());

    }

    @Test
    void toResource_members() {
        //given
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        userInfoModel.setId(UUID.randomUUID().toString());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = TestUtils.mockInstance(new User());
        model.setCreatedBy(userModel);
        model.setModifiedBy(userModel);
        Instant now = Instant.now();
        model.setModifiedAt(now);
        model.setCreatedAt(now);
        //when
        UserGroupResource resource = GroupMapper.toResource(model);
        //then
        assertEquals(model.getMembers().get(0).getId(), resource.getMembers().get(0).getId().toString());
        assertEquals(model.getMembers().get(0).getUser().getName().getValue(), resource.getMembers().get(0).getName());
        assertEquals(model.getMembers().get(0).getUser().getFamilyName().getValue(), resource.getMembers().get(0).getSurname());
        assertEquals(model.getMembers().get(0).getUser().getEmail().getValue(), resource.getMembers().get(0).getEmail());
        assertEquals(model.getMembers().get(0).getRole(), resource.getMembers().get(0).getRole());
        assertEquals(model.getMembers().get(0).getStatus(), resource.getMembers().get(0).getStatus());
    }

    @Test
    void toResource_nullPlainResource() {
        //given
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        userInfoModel.setId(UUID.randomUUID().toString());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = null;
        model.setCreatedBy(userModel);
        model.setModifiedBy(userModel);
        //when
        UserGroupResource resource = GroupMapper.toResource(model);
        //then
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getDescription(), resource.getDescription());
        assertEquals(model.getStatus(), resource.getStatus());
        assertEquals(model.getInstitutionId(), resource.getInstitutionId());
        assertEquals(model.getProductId(), resource.getProductId());
        assertEquals(model.getStatus(), resource.getStatus());
        assertNull(resource.getCreatedBy());
        assertNull(resource.getModifiedBy());
        TestUtils.reflectionEqualsByName(resource, model);
    }

    @Test
    void toPlainGroupResource_null() {
        //given
        UserGroupInfo model = null;
        //when
        UserGroupPlainResource resource = GroupMapper.toPlainGroupResource(model);
        //then
        assertNull(resource);
    }

    @Test
    void toPlainGroupResource_notNull() {
        //given
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo());
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = TestUtils.mockInstance(new User());
        userModel.setId(UUID.randomUUID().toString());
        model.setCreatedBy(userModel);
        model.setModifiedBy(userModel);
        Instant now = Instant.now();
        model.setModifiedAt(now);
        model.setCreatedAt(now);
        //when
        UserGroupPlainResource resource = GroupMapper.toPlainGroupResource(model);
        //then
        assertNotNull(resource);
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getProductId(), resource.getProductId());
        assertEquals(model.getModifiedAt(), resource.getModifiedAt());
        assertEquals(model.getCreatedAt(), resource.getCreatedAt());
        assertEquals(model.getCreatedBy().getId(), resource.getCreatedBy().toString());
        assertEquals(model.getModifiedBy().getId(), resource.getModifiedBy().toString());
        assertEquals(model.getMembers().size(), resource.getMembersCount());
        assertEquals(model.getInstitutionId(), resource.getInstitutionId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getDescription(), resource.getDescription());
    }

    @Test
    void toPlainGroupResource_nullModifiedBy() {
        //given
        UserGroupInfo model = TestUtils.mockInstance(new UserGroupInfo(), "setModifiedBy");
        UserInfo userInfoModel = TestUtils.mockInstance(new UserInfo());
        ProductInfo productInfo = TestUtils.mockInstance(new ProductInfo());
        List<RoleInfo> roleInfos = List.of(TestUtils.mockInstance(new RoleInfo()));
        Map<String, ProductInfo> productInfoMap = new HashMap<>();
        productInfo.setRoleInfos(roleInfos);
        productInfoMap.put(productInfo.getId(), productInfo);
        userInfoModel.setProducts(productInfoMap);
        model.setMembers(List.of(userInfoModel));
        User userModel = TestUtils.mockInstance(new User());
        userModel.setId(UUID.randomUUID().toString());
        model.setCreatedBy(userModel);
        Instant now = Instant.now();
        model.setCreatedAt(now);
        //when
        UserGroupPlainResource resource = GroupMapper.toPlainGroupResource(model);
        //then
        assertNotNull(resource);
        assertEquals(model.getId(), resource.getId());
        assertEquals(model.getProductId(), resource.getProductId());
        assertEquals(model.getCreatedAt(), resource.getCreatedAt());
        assertEquals(model.getCreatedBy().getId(), resource.getCreatedBy().toString());
        assertNull(resource.getModifiedBy());
        assertNull(resource.getModifiedAt());
        assertEquals(model.getMembers().size(), resource.getMembersCount());
        assertEquals(model.getInstitutionId(), resource.getInstitutionId());
        assertEquals(model.getName(), resource.getName());
        assertEquals(model.getDescription(), resource.getDescription());
    }

}