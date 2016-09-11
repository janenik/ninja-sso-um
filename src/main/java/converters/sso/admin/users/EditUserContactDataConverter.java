package converters.sso.admin.users;

import converters.Converter;
import dto.sso.admin.users.UserEditContactDataDto;
import models.sso.User;
import org.dozer.Mapper;
import services.sso.CountryService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Converter for {@link UserEditContactDataDto}.
 */
@Singleton
public class EditUserContactDataConverter implements Converter<User, UserEditContactDataDto> {

    /**
     * DTO mapper.
     */
    final Mapper mapper;

    /**
     * Country service.
     */
    final CountryService countryService;

    /**
     * Constructs converter.
     *
     * @param mapper Dozer's mapper.
     * @param countryService Country service.
     */
    @Inject
    public EditUserContactDataConverter(Mapper mapper, CountryService countryService) {
        this.mapper = mapper;
        this.countryService = countryService;
    }

    @Override
    public User fromDto(UserEditContactDataDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public UserEditContactDataDto fromEntity(User entity) {
        UserEditContactDataDto dto =  mapper.map(entity, UserEditContactDataDto.class);
        if (entity.getCountry() != null) {
            dto.setCountryId(entity.getCountry().getIso());
        }
        return dto;
    }

    @Override
    public User update(User entity, UserEditContactDataDto dto) {
        mapper.map(dto, entity);
        entity.setCountry(countryService.get(dto.getCountryId()));
        return entity;
    }
}
