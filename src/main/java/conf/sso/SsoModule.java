package conf.sso;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ninja.utils.NinjaProperties;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import services.sso.token.AesPasswordBasedEncryptor;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import java.util.Collections;
import java.util.List;

/**
 * SSO module.
 */
public class SsoModule extends AbstractModule {

    @Override
    protected void configure() {
        // Configure password and expirable token encryptors.
        bind(ExpirableTokenEncryptor.class);

        // Configure Dozer.
        bind(Mapper.class).toInstance(new DozerBeanMapper());

        // Configure start up actions.
        bind(SsoStartupActions.class);
    }

    /**
     * Provides password based encryptor.
     *
     * @param properties Properties.
     * @return Password based encryptor.
     */
    @Provides
    PasswordBasedEncryptor providesPasswordBasedEncryptor(NinjaProperties properties) {
        char[] key = properties.getOrDie("application.sso.tokens.encryption.aes.key").toCharArray();
        short strength = Short.valueOf(
                properties.getWithDefault("application.sso.tokens.encryption.aes.strength", "128"));
        return new AesPasswordBasedEncryptor(key, strength);
    }

    /**
     * Provides a list with allowed continue URL prefixes. Includes ${application.baseUrl}.
     *
     * @param properties Application properties.
     * @return List of allowed continue URL prefixes, including self.
     */
    @Provides
    @Named("allowedContinueUrls")
    @Singleton
    List<String> provideAllowedRedirects(NinjaProperties properties) {
        String baseUrl = properties.get("application.baseUrl");
        String[] urls = properties.getStringArray("application.sso.allowedContinueUrls");
        if (urls == null) {
            urls = new String[0];
        }
        List<String> allowedRedirects = Lists.newArrayListWithCapacity(urls.length + 1);
        allowedRedirects.add(baseUrl);
        for (String url : urls) {
            url = Strings.nullToEmpty(url).trim();
            if (!url.isEmpty()) {
                allowedRedirects.add(url);
            }
        }
        return Collections.unmodifiableList(allowedRedirects);
    }
}