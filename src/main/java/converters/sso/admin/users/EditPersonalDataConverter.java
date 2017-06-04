package converters.sso.admin.users;

import converters.Converter;
import dto.sso.admin.users.EditPersonalDataDto;
import models.sso.User;
import models.sso.UserGender;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;

/**
 * Converter for {@link EditPersonalDataDto}.
 */
@Singleton
public class EditPersonalDataConverter implements Converter<User, EditPersonalDataDto> {

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
    public EditPersonalDataConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User fromDto(EditPersonalDataDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public EditPersonalDataDto fromEntity(User entity) {
        EditPersonalDataDto dto = mapper.map(entity, EditPersonalDataDto.class);
        dto.setBirthDay(entity.getDateOfBirth().getDayOfMonth());
        dto.setBirthMonth(entity.getDateOfBirth().getMonthValue());
        dto.setBirthYear(entity.getDateOfBirth().getYear());
        dto.setGender(entity.getGender().toString());
        return dto;
    }

    @Override
    public User update(User entity, EditPersonalDataDto dto) {
        mapper.map(dto, entity);
        entity.setGender(UserGender.valueOf(dto.getGender()));
        entity.setDateOfBirth(LocalDate.of(dto.getBirthYear(), dto.getBirthMonth(), dto.getBirthDay()));
        return entity;
    }
}
