package converters.sso.admin.users;

import converters.Converter;
import dto.sso.admin.users.EditAccessDto;
import models.sso.User;
import models.sso.UserConfirmationState;
import models.sso.UserRole;
import models.sso.UserSignInState;
import org.dozer.Mapper;

import javax.inject.Inject;

/**
 * Data converter for user role DTO.
 */
public class EditAccessConverter implements Converter<User, EditAccessDto> {

    /**
     * DTO mapper.
     */
    private final Mapper mapper;

    /**
     * Constructs converter.
     *
     * @param mapper Dozer's mapper.
     */
    @Inject
    public EditAccessConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User fromDto(EditAccessDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public EditAccessDto fromEntity(User entity) {
        EditAccessDto dto = mapper.map(entity, EditAccessDto.class);
        dto.setRole(entity.getRole().name());
        dto.setSignInState(entity.getSignInState().name());
        dto.setConfirmationState(entity.getConfirmationState().name());
        return dto;
    }

    @Override
    public User update(User entity, EditAccessDto dto) {
        mapper.map(dto, entity);
        entity.setRole(UserRole.fromString(dto.getRole()));
        entity.setSignInState(UserSignInState.fromString(dto.getSignInState()));
        entity.setConfirmationState(UserConfirmationState.fromString(dto.getConfirmationState()));
        return entity;
    }
}
