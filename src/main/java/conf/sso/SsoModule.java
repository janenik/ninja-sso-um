package conf.sso;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import controllers.annotations.AllowedContinueUrls;
import controllers.annotations.ApplicationPolicy;
import controllers.annotations.BrowserPolicy;
import controllers.sso.auth.policy.AppendAuthTokenPolicy;
import controllers.sso.auth.policy.DeviceAuthPolicy;
import ninja.utils.NinjaProperties;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import services.sso.annotations.ExclusionDictionary;
import services.sso.annotations.ExclusionSubstrings;
import services.sso.token.AesPasswordBasedEncryptor;
import services.sso.token.ExpirableTokenEncryptor;
import services.sso.token.PasswordBasedEncryptor;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * SSO module.
 */
public class SsoModule extends AbstractModule {

    /**
     * Exclusion dictionaries directory.
     */
    private static final String EXCLUSION_DICTIONARIES_DIRECTORY = "dictionaries";

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
    @AllowedContinueUrls
    @Singleton
    List<String> provideAllowedContinueUrls(NinjaProperties properties) {
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

    /**
     * Provides device authorization policy (how to pass authentication tokens to browser and mobile devices).
     *
     * @param properties Properties.
     * @param logger Logger.
     * @return Device authentication policy.
     */
    @Provides
    @Singleton
    DeviceAuthPolicy provideDeviceAuthPolicy(NinjaProperties properties, Logger logger) {
        String property = "application.sso.device.auth.policy";
        String policy = properties.getWithDefault(property, DeviceAuthPolicy.AUTO.toString());
        try {
            return DeviceAuthPolicy.valueOf(policy);
        } catch (Exception e) {
            logger.error("Error while parsing " + property + ": " + policy, e);
            return DeviceAuthPolicy.AUTO;
        }
    }

    /**
     * Provides browser append token policy.
     *
     * @param properties Properties.
     * @param logger Logger.
     * @return Desktop append token policy.
     */
    @Provides
    @BrowserPolicy
    @Singleton
    AppendAuthTokenPolicy provideBrowserAppendAuthTokenPolicy(NinjaProperties properties, Logger logger) {
        String property = "application.sso.device.auth.policy.append.browser";
        String policy = properties.getWithDefault(property, AppendAuthTokenPolicy.COOKIE.toString());
        try {
            return AppendAuthTokenPolicy.valueOf(policy);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("Error while parsing " + property + ": " + policy, e);
            return AppendAuthTokenPolicy.COOKIE;
        }
    }


    /**
     * Provides standalone application append token policy.
     *
     * @param properties Properties.
     * @param logger Logger.
     * @return Application append token policy.
     */
    @Provides
    @ApplicationPolicy
    @Singleton
    AppendAuthTokenPolicy provideApplicationAppendAuthTokenPolicy(NinjaProperties properties, Logger logger) {
        String property = "application.sso.device.auth.policy.append.application";
        String policy = properties.getWithDefault(property, AppendAuthTokenPolicy.URL_PARAM.toString());
        try {
            return AppendAuthTokenPolicy.valueOf(policy);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("Error while parsing " + property + ": " + policy, e);
            return AppendAuthTokenPolicy.URL_PARAM;
        }
    }

    /**
     * Provides default date/time formatted for application.
     *
     * @param properties Application properties.
     * @return Date-time formatter.
     */
    @Provides
    @Singleton
    DateTimeFormatter provideDateTimeFormatter(NinjaProperties properties) {
        String format = properties.getOrDie("application.sso.admin.dateTimeFormat");
        return DateTimeFormatter.ofPattern(format);
    }

    /**
     * Provides exclusion substrings for username validation.
     *
     * @param dictionary All exclusion dictionary.
     * @param properties Application properties.
     * @param logger Logger.
     * @return A set of exclusion keywords.
     */
    @Provides
    @ExclusionSubstrings
    @Singleton
    Set<String> provideExclusionSubstrings(
            @ExclusionDictionary Set<String> dictionary,
            NinjaProperties properties,
            Logger logger) throws IOException {
        int minKeywordLengthForSubstring =
                properties.getIntegerWithDefault("application.sso.minKeywordLengthForSubstringExclusion", 5);
        Set<String> exclusionSubstrings = new HashSet<>();
        Pattern starPattern = Pattern.compile("\\*");
        for (String keyword : dictionary) {
            if (keyword.startsWith("*") || keyword.endsWith("*")) {
                exclusionSubstrings.add(starPattern.matcher(keyword).replaceAll(""));
            } else if (keyword.length() >= minKeywordLengthForSubstring) {
                exclusionSubstrings.add(keyword);
            }
        }
        logger.info("Exclusion substrings set contains {} substrings.", exclusionSubstrings.size());
        return Collections.unmodifiableSet(exclusionSubstrings);
    }

    /**
     * Provides exclusion dictionary for username validation.
     *
     * @param logger Logger.
     * @return A set of exclusion keywords.
     */
    @Provides
    @ExclusionDictionary
    @Singleton
    Set<String> provideExclusionDictionary(Logger logger) throws IOException {
        List<String> files = getResourcesWithSubpath(EXCLUSION_DICTIONARIES_DIRECTORY, logger);
        logger.info("Found {} dictionary resources.", files);
        Set<String> result = new HashSet<>();
        for (String file : files) {
            result.addAll(getResourceLines(file, logger));
        }
        logger.info("Exclusion dictionary contains {} keywords.", result.size());
        return Collections.unmodifiableSet(result);
    }

    /**
     * Reads non-empty lines from given resource relative to the current class.
     * Excludes lines that start with '#' character.
     *
     * @param resource Resource to read.
     * @param logger Logger.
     * @return List of lines from resource.
     */
    private static List<String> getResourceLines(String resource, Logger logger) {
        InputStream is = null;
        try {
            Path existingFile = Paths.get(resource);
            if (Files.isRegularFile(existingFile)) {
                logger.info("Reading file {}", existingFile);
                return Files.readAllLines(existingFile);
            }
            is = SsoModule.class.getResourceAsStream(resource);
            if (is == null) {
                is = ClassLoader.getSystemResourceAsStream(resource);
                if (is == null) {
                    logger.info("Resource {} not found.", resource);
                    return Collections.emptyList();
                }
            }
            logger.info("Reading resource {}", resource);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
            return buffer.lines()
                    .filter(s -> s != null && !s.isEmpty() && !s.startsWith("#"))
                    .collect(Collectors.toList());
        } catch (Exception ioe) {
            logger.error("Error while reading resource {}.", resource, ioe);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                // Ignore closing exception.
            }
        }
        return Collections.emptyList();
    }

    /**
     * Reads resources which contain sub path.
     *
     * @param subPath Resource sub path to search for.
     * @param logger Logger.
     * @return Resources with given sub path..
     */
    private static List<String> getResourcesWithSubpath(String subPath, Logger logger) {
        return getModuleResources(path -> path.contains(subPath), logger);
    }

    /**
     * Reads resources for current module.
     *
     * @param predicate Predicate for entries' tests.
     * @param logger Logger.
     * @return List of module resources.
     */
    private static List<String> getModuleResources(Predicate<String> predicate, Logger logger) {
        CodeSource cs = SsoModule.class.getProtectionDomain().getCodeSource();
        if (cs == null) {
            logger.info("Code source for {} not found.", SsoModule.class.getName());
            return Collections.emptyList();
        }
        String packagePath = SsoModule.class.getPackage().getName().replaceAll("\\.", File.separator);
        try {
            URL codeSourceLocation = cs.getLocation();
            if (codeSourceLocation.getFile().endsWith(".jar")) {
                logger.info("Reading jar: {}.", codeSourceLocation.getFile());
                ZipInputStream zip = new ZipInputStream(codeSourceLocation.openStream());
                List<String> zipFileEntries = new ArrayList<>();
                while (true) {
                    ZipEntry entry = zip.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    String zipFilePath = entry.getName();
                    if (zipFilePath.startsWith(packagePath) && predicate.test(zipFilePath)) {
                        zipFileEntries.add(zipFilePath);
                    }
                }
                return zipFileEntries;
            } else {
                Path root = Paths.get(codeSourceLocation.getFile(), packagePath);
                logger.info("Reading directory: {}.", root);
                return Files.walk(root)
                        .map(Object::toString)
                        .filter(predicate)
                        .collect(Collectors.toList());
            }
        } catch (IOException ioe) {
            logger.error("Error reading resource {}.", SsoModule.class.getProtectionDomain().getCodeSource(), ioe);
            return Collections.emptyList();
        }
    }
}