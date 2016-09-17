package controllers.sso.admin.users;

import com.google.inject.persist.Transactional;
import controllers.sso.filters.ApplicationErrorHtmlFilter;
import controllers.sso.filters.AuthenticationFilter;
import controllers.sso.filters.IpAddressFilter;
import controllers.sso.filters.LanguageFilter;
import controllers.sso.filters.RequireAdminPrivelegesFilter;
import controllers.sso.web.UrlBuilder;
import converters.sso.admin.users.EditContactDataConverter;
import dto.sso.admin.users.EditContactDataDto;
import models.sso.Country;
import models.sso.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import services.sso.CountryService;
import services.sso.UserEventService;
import services.sso.UserService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Edit user personal data controller.
 */
@Singleton
@FilterWith({
        ApplicationErrorHtmlFilter.class,
        LanguageFilter.class,
        IpAddressFilter.class,
        AuthenticationFilter.class,
        RequireAdminPrivelegesFilter.class
})
public class EditContactDataController extends
        EditAbstractController<EditContactDataConverter, EditContactDataDto> {

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
    public EditContactDataController(
            UserService userService,
            UserEventService userEventService,
            CountryService countryService,
            EditContactDataConverter converter,
            Provider<UrlBuilder> urlBuilderProvider,
            @Named("htmlAdminSecureHeaders") Provider<Result> htmlAdminSecureHeadersProvider,
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
            @JSR303Validation EditContactDataDto dto,
            Validation validation,
            Context context) {
        return super.updateUserOrRedirectToList(userId, dto, context, validation);
    }

    @Override
    protected String validate(User user, EditContactDataDto dto) {
        // Check for existing email.
        User existingUserWithEmail = userService.getByEmail(dto.getEmail());
        if (existingUserWithEmail != null && !existingUserWithEmail.equals(user)) {
            return "emailDuplicate";
        }
        // Fetch country.
        Country country = countryService.get(dto.getCountryId());
        if (country == null) {
            return "country";
        }
        return null;
    }

    @Override
    protected String getSuccessRedirectUrl(User user, Context context) {
        return urlBuilderProvider.get()
                .getAdminEditContactDataUrl(user.getId(), context.getParameter("query"), context.getParameter("page"));
    }

    @Override
    protected String getTemplate() {
        return "views/sso/admin/users/edit-contact.ftl.html";
    }
}
