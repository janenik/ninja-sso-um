package converters.sso.admin.users;

import converters.Converter;
import dto.sso.admin.users.EditUserRoleDto;
import models.sso.User;
import models.sso.UserRole;
import org.dozer.Mapper;

import javax.inject.Inject;

/**
 * Data converter for user role DTO.
 */
public class EditUserRoleConverter implements Converter<User, EditUserRoleDto> {

    /**
     * DTO mapper.
     */
    final Mapper mapper;

    /**
     * Constructs converter.
     *
     * @param mapper Dozer's mapper.
     */
    @Inject
    public EditUserRoleConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User fromDto(EditUserRoleDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public EditUserRoleDto fromEntity(User entity) {
        EditUserRoleDto dto = mapper.map(entity, EditUserRoleDto.class);
        dto.setRole(entity.getRole().name());
        return dto;
    }

    @Override
    public User update(User entity, EditUserRoleDto dto) {
        mapper.map(dto, entity);
        entity.setRole(UserRole.fromString(dto.getRole()));
        return entity;
    }
}
