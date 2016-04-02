package services.sso.mail;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.utils.NinjaProperties;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Email service with templating.
 */
@Singleton
public class EmailService {

    public static final String DEFAULT_EMAIL_TEMPLATE_PACKAGE = "/views/sso/mail/";

    /**
     * Mail provider.
     */
    final Provider<Mail> mailProvider;

    /**
     * Post office.
     */
    final Postoffice postoffice;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Configuration for freemarker template.
     */
    final Configuration templateConfiguration;

    /**
     * Constructs templating service.
     *
     * @param mailProvider Mail provider.
     * @param postoffice Post office reference.
     * @param properties Application properties.
     */
    @Inject
    public EmailService(Provider<Mail> mailProvider,
                        Postoffice postoffice,
                        NinjaProperties properties) {
        this.mailProvider = mailProvider;
        this.postoffice = postoffice;
        this.properties = properties;

        this.templateConfiguration = new Configuration(Configuration.getVersion());
        this.templateConfiguration.setTemplateLoader(new ClassTemplateLoader(this.getClass(),
                properties.getWithDefault("smtp.templates.basepath", DEFAULT_EMAIL_TEMPLATE_PACKAGE)));
    }

    /**
     * Sends email to recipient with subject, template name, data and attachments. In dev and test modes logs the mail
     * message into the log.
     *
     * @param to To.
     * @param subject Subject.
     * @param templateName Template name in file system.
     * @param data Data for template.
     * @param attachments Attachments.
     * @throws MessagingException In case of error while sending a message.
     * @throws TemplateException In case of error with template syntax or data.
     */
    public void send(String to, String subject, String templateName, Map<String, Object> data,
                     Object... attachments) throws MessagingException, TemplateException {
        String from = properties.getWithDefault("smtp.from.default", "user@localhost");
        try {
            send(from, to, subject, templateName, data, attachments);
        } catch (IOException e) {
            throw new MessagingException("Error.", e);
        }
    }

    /**
     * Sends email from recipient to recipient with subject, template name, data and attachments. In dev and test modes
     * logs the mail message into the log.
     *
     * @param from From.
     * @param to To.
     * @param subject Subject.
     * @param templateName Template name in file system.
     * @param data Data for template.
     * @param attachments Attachments.
     * @throws MessagingException In case of error.
     * @throws IOException In case of error.
     * @throws TemplateException In case of error.
     */
    public void send(String from, String to, String subject, String templateName, Map<String, Object> data,
                     Object... attachments) throws MessagingException, IOException, TemplateException {
        Template template = templateConfiguration.getTemplate(templateName);
        data.put("config", properties);
        StringWriter sw = new StringWriter();
        template.process(data, sw);
        String body = sw.toString();

        throw new UnsupportedOperationException("Finish");
        //postoffice.sendHtml(from, to, subject, body, attachments);
    }
}