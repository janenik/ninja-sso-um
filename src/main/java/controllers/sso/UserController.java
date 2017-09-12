package controllers.sso;

import com.google.inject.persist.Transactional;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.rest.RestResponse;
import dto.sso.UserDto;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.metrics.Timed;
import ninja.utils.NinjaProperties;
import org.dozer.Mapper;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Rest controller to build user JSON response.
 */
@Singleton
@FilterWith({
        AuthenticationFilter.class
})
public class UserController {

    /**
     * User service.
     */
    final UserService userService;

    /**
     * DTO mapper.
     */
    final Mapper dtoMapper;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Constructs user controller.
     *
     * @param userService User service.
     * @param dtoMapper DTO mapper.
     * @param properties Application properties.
     */
    @Inject
    public UserController(UserService userService, Mapper dtoMapper, NinjaProperties properties) {
        this.userService = userService;
        this.dtoMapper = dtoMapper;
        this.properties = properties;
    }

    @Timed
    @Transactional
    public Result user(Context context) {
        boolean authenticated = (boolean) context.getAttribute(AuthenticationFilter.USER_AUTHENTICATED);
        if (!authenticated) {
            return Results.json().render(RestResponse.notAuthorized("Not authenticated."));
        }

        User user = userService.get((long) context.getAttribute(AuthenticationFilter.USER_ID));
        UserDto userDto = dtoMapper.map(user, UserDto.class);
        userDto.setCountry(user.getCountry().getIso());
        userDto.setAge(ChronoUnit.YEARS.between(user.getDateOfBirth(), LocalDate.now()));
        return Results.json()
                .render(RestResponse.newResponse(userDto));
    }
}
