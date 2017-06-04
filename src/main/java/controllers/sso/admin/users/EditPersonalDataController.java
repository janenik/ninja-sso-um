package controllers.sso.admin.users;

import com.google.inject.persist.Transactional;
import controllers.annotations.SecureHtmlHeadersForAdmin;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.filters.XsrfTokenFilter;
import controllers.sso.web.UrlBuilder;
import converters.sso.admin.users.EditPersonalDataConverter;
import dto.sso.admin.users.EditPersonalDataDto;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
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
 * Edit user personal data controller.
 */
@Singleton
@FilterWith({
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class,
        XsrfTokenFilter.class
})
public class EditPersonalDataController extends
        EditAbstractController<EditPersonalDataConverter, EditPersonalDataDto> {

    /**
     * Template.
     */
    private static final String TEMPLATE = "views/sso/admin/users/edit-personal.ftl.html";

    /**
     * Constructs controller.
     *
     * @param userService User service.
     * @param countryService Country service.
     * @param userEventService User event service.
     * @param urlBuilderProvider URL builder provider.
     * @param htmlAdminSecureHeadersProvider HTML with secure headers provider for admin.
     * @param properties Application properties.
     */
    @Inject
    public EditPersonalDataController(
            UserService userService,
            CountryService countryService,
            UserEventService userEventService,
            EditPersonalDataConverter converter,
            Provider<UrlBuilder> urlBuilderProvider,
            @SecureHtmlHeadersForAdmin Provider<Result> htmlAdminSecureHeadersProvider,
            NinjaProperties properties) {
        super(userService, userEventService, countryService, converter, urlBuilderProvider,
                htmlAdminSecureHeadersProvider, properties);
    }

    @Transactional
    public Result get(@PathParam("userId") long userId, Context context) {
        return super.renderUserOrRedirectToList(userId, context);
    }

    @Transactional
    public Result post(
            @PathParam("userId") long userId,
            @JSR303Validation EditPersonalDataDto dto,
            Context context,
            FlashScope flashScope,
            Validation validation) {
        return super.updateUserOrRedirectToList(userId, dto, context, flashScope, validation);
    }

    @Override
    protected String validate(User user, EditPersonalDataDto dto) {
        if (!dto.isValidBirthday()) {
            return "birthDay";
        }
        if (!user.getUsername().equals(dto.getUsername())) {
            // Check for existing username if username is about to change.
            User existingUserWithUsername = userService.getByUsername(dto.getUsername());
            if (existingUserWithUsername != null && !existingUserWithUsername.equals(user)) {
                return "usernameDuplicate";
            }
        }
        return null;
    }

    @Override
    protected String getSuccessRedirectUrl(User user, Context context) {
        return urlBuilderProvider.get()
                .getAdminEditPersonalDataUrl(user.getId(), context.getParameter("query"), context.getParameter("page"));
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }
}
