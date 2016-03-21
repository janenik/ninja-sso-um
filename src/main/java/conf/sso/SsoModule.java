package conf.sso;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import ninja.utils.NinjaProperties;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import services.sso.token.AesPasswordBasedEncryptor;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

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
}