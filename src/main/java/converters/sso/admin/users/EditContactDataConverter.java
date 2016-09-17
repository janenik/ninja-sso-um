package converters.sso.admin.users;

import converters.Converter;
import dto.sso.admin.users.EditContactDataDto;
import models.sso.User;
import org.dozer.Mapper;
import services.sso.CountryService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Converter for {@link EditContactDataDto}.
 */
@Singleton
public class EditContactDataConverter implements Converter<User, EditContactDataDto> {

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
    public EditContactDataConverter(Mapper mapper, CountryService countryService) {
        this.mapper = mapper;
        this.countryService = countryService;
    }

    @Override
    public User fromDto(EditContactDataDto dto) {
        return mapper.map(dto, User.class);
    }

    @Override
    public EditContactDataDto fromEntity(User entity) {
        EditContactDataDto dto =  mapper.map(entity, EditContactDataDto.class);
        if (entity.getCountry() != null) {
            dto.setCountryId(entity.getCountry().getIso());
        }
        return dto;
    }

    @Override
    public User update(User entity, EditContactDataDto dto) {
        mapper.map(dto, entity);
        entity.setCountry(countryService.get(dto.getCountryId()));
        return entity;
    }
}
