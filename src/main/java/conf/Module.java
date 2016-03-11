/**
 * Copyright (C) 2012 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ninja.utils.NinjaProperties;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import services.sso.token.AesPasswordBasedEncryptor;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

@Singleton
public class Module extends AbstractModule {

    @Inject
    NinjaProperties properties;

    protected void configure() {
        bind(StartupActions.class);

        // Configure expirable token encryptor.
        bind(ExpirableTokenEncryptor.class);

        // Configure Dozer.
        bind(Mapper.class).toInstance(new DozerBeanMapper());
    }

    private void configureSso() {
    }

    private void configureUserManagement() {
    }

    @Provides
    PasswordBasedEncryptor passwordBasedEncryptor() {
        char[] key = properties.getOrDie("application.tokens.encryption.aes.key").toCharArray();
        short strength = Short.valueOf(
                properties.getWithDefault("application.tokens.encryption.aes.strength", "128"));
        return new AesPasswordBasedEncryptor(key, strength);
    }
}
