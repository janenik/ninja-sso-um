package converters.sso.admin.users;

import converters.Converter;
import dto.sso.admin.users.EditUserPersonalDataDto;
import models.sso.User;
import models.sso.UserGender;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;

/**
 * Converter for {@link EditUserPersonalDataDto}.
 */
@Singleton
public class EditUserPersonalDataConverter implements Converter<User, EditUserPersonalDataDto> {

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
    public EditUserPersonalDataConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User fromDto(EditUserPersonalDataDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public EditUserPersonalDataDto fromEntity(User entity) {
        EditUserPersonalDataDto dto = mapper.map(entity, EditUserPersonalDataDto.class);
        dto.setBirthDay(entity.getDateOfBirth().getDayOfMonth());
        dto.setBirthMonth(entity.getDateOfBirth().getMonthValue());
        dto.setBirthYear(entity.getDateOfBirth().getYear());
        dto.setGender(entity.getGender().toString());
        return dto;
    }

    @Override
    public User update(User entity, EditUserPersonalDataDto dto) {
        mapper.map(dto, entity);
        entity.setGender(UserGender.valueOf(dto.getGender()));
        entity.setDateOfBirth(LocalDate.of(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay()));
        return entity;
    }
}
