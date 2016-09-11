package converters;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter that helps to convert entities into Data Transfer Objects (DTO) and back.
 *
 * @param <E> Entity type.
 * @param <D> DTO type.
 */
public interface Converter<E, D> {

    /**
     * Converts given DTO object into entity.
     *
     * @param dto Data Transfer Object.
     * @return Entity.
     */
    E fromDto(D dto);

    /**
     * Converts given entity into Data Transfer Object.
     *
     * @param entity Entity.
     * @return Data Transfer Object.
     */
    D fromEntity(E entity);

    /**
     * Updates given entity with data from Data Transfer Object.
     *
     * @param entity Entity to update.
     * @param dto DTO to read data from.
     * @return Updated entity (same as argument).
     */
    E update(E entity, D dto);

    /**
     * Converts given entities into Data Transfer Objects.
     *
     * @param entities Entities to convert.
     * @return List of Data Transfer Objects.
     */
    default List<D> fromEntities(Collection<E> entities) {
        return entities.stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Converts given Data Transfer Objects into entities.
     *
     * @param dtos Data Transfer Objects to convert.
     * @return List of entities.
     */
    default List<E> fromDtos(Collection<D> dtos) {
        return dtos.stream()
                .map(this::fromDto)
                .collect(Collectors.toList());
    }
}
