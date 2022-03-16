package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.dashboard.connector.model.groups.CreateUserGroup;
import it.pagopa.selfcare.dashboard.web.model.user_groups.CreateUserGroupDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.validation.ValidationException;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    @Test
    void fromDto_null() {
        //given
        CreateUserGroupDto dto = null;
        //when
        CreateUserGroup model = GroupMapper.fromDto(dto);
        //then
        assertNull(model);
    }

    @Test
    void fromDto() {
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
    void fromDto_nullMembersList() {
        //given
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        //when
        Executable executable = () -> GroupMapper.fromDto(dto);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Members list must not be null", e.getMessage());
    }

}