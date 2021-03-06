package controllers.sso.admin.users;

import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeadersForAdmin;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.filters.XsrfTokenFilter;
import controllers.sso.web.UrlBuilder;
import converters.sso.admin.users.EditAccessConverter;
import dto.sso.admin.users.EditAccessDto;
import models.sso.User;
import models.sso.UserRole;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.metrics.Timed;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import services.sso.CountryService;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Edit user role controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class,
        XsrfTokenFilter.class
})
public class EditAccessController extends EditAbstractController<EditAccessConverter, EditAccessDto> {

    /**
     * Template.
     */
    private static final String TEMPLATE = "views/sso/admin/users/edit-access.ftl.html";

    /**
     * Constructs controller.
     *
     * @param userService User service.
     * @param countryService Country service.
     * @param userEventService User event service.
     * @param converter Contact data converter.
     * @param urlBuilderProvider URL builder provider.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    @Inject
    public EditAccessController(
            UserService userService,
            UserEventService userEventService,
            CountryService countryService,
            EditAccessConverter converter,
            Provider<UrlBuilder> urlBuilderProvider,
            @SecureHtmlHeadersForAdmin Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        super(userService, userEventService, countryService, converter, urlBuilderProvider,
                htmlAdminSecureHeadersProvider, properties);
    }

    /**
     * Produces Edit Access form.
     *
     * @param userId User id whose access to edit.
     * @param context Request context.
     * @return Result with Edit Access form.
     */
    @Timed
    @Transactional
    public Result get(@PathParam("userId") long userId, Context context) {
        return super.renderUserOrRedirectToList(userId, context);
    }

    /**
     * Processes POST request from Edit Access form. Updates user with necessary information.
     *
     * @param userId User id whose access to edit.
     * @param dto Form Data Transfer Object.
     * @param context Request context.
     * @param flashScope Flash scope.
     * @param validation Validation.
     * @return Result with Edit Access form.
     */
    @Timed
    @Transactional
    public Result post(
            @PathParam("userId") long userId,
            @JSR303Validation EditAccessDto dto,
            Context context,
            FlashScope flashScope,
            Validation validation) {
        return super.updateUserOrRedirectToList(userId, dto, context, flashScope, validation);
    }

    @Override
    protected String validate(User user, EditAccessDto editAccessDto) {
        try {
            UserRole.valueOf(editAccessDto.getRole());
        } catch (IllegalArgumentException iae) {
            return "role";
        }
        return null;
    }

    @Override
    protected String getSuccessRedirectUrl(User user, Context context) {
        return urlBuilderProvider.get()
                .getAdminEditUserRoleUrl(user.getId(), context.getParameter("query"), context.getParameter("page"));
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }
}
