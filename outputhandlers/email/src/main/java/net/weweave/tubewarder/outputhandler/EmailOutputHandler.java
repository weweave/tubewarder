package net.weweave.tubewarder.outputhandler;

import net.weweave.tubewarder.outputhandler.api.*;
import net.weweave.tubewarder.outputhandler.api.Address;
import net.weweave.tubewarder.outputhandler.api.configoption.*;
import org.apache.commons.validator.GenericValidator;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

@OutputHandler(id="EMAIL", name="Email")
public class EmailOutputHandler implements IOutputHandler {
    private static final Logger LOG = Logger.getLogger(EmailOutputHandler.class.getName());

    @Override
    public void process(Config config, SendItem item) throws TemporaryProcessingException, PermanentProcessingException {
        Session session = getSession(config);
        try {
            MimeMessage message = createMimeMessage(session, item.getSender());
            MimeMultipart multipart = prepareMessage(config, message, item.getRecipient(), item.getSubject(), item.getContent());
            appendAttachments(multipart, item.getAttachments());
            message.setContent(multipart);
            sendMail(config, session, message);
        } catch (UnknownHostException e) {
            throw new TemporaryProcessingException(e.getMessage());
        } catch (MessagingException e) {
            throw new TemporaryProcessingException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new PermanentProcessingException(e.getMessage());
        }
    }

    @Override
    public List<OutputHandlerConfigOption> getConfigOptions() {
        List<OutputHandlerConfigOption> options = new ArrayList<>();
        options.add(new StringConfigOption("smtpServer", "SMTP Server", true, ""));
        options.add(new IntConfigOption("port", "Port", true, 25));
        options.add(new BoolConfigOption("auth", "Authentication required", false, false));
        options.add(new StringConfigOption("username", "Username", false, ""));
        options.add(new StringConfigOption("password", "Password", false, ""));
        SelectConfigOption security = new SelectConfigOption("security", "Security", true, "NONE");
        security.addOption("NONE", "None");
        security.addOption("SSL", "SSL");
        security.addOption("TLS", "TLS");
        options.add(security);
        options.add(new StringConfigOption("contentType", "Content Type", true, "text/plain"));
        return options;
    }

    @Override
    public void checkConfig(Config config) throws InvalidConfigException {
        if (GenericValidator.isBlankOrNull(config.getString("smtpServer"))) {
            throw new FieldRequiredException("smtpServer");
        }
        if (config.getInt("port") == null || config.getInt("port") <= 0 || config.getInt("port") > 65535) {
            throw new FieldInvalidException("port");
        }
        if (config.getBool("auth") && GenericValidator.isBlankOrNull(config.getString("username"))) {
            throw new FieldRequiredException("username");
        }
        if (!("NONE".equals(config.getString("security")) || "SSL".equals(config.getString("security")) || "TLS".equals(config.getString("security")))) {
            throw new FieldInvalidException("security");
        }
    }

    @Override
    public void checkRecipientAddress(Address address) throws InvalidAddessException {
        LOG.fine("Checking recipient address: " + address.getAddress());
        if (GenericValidator.isBlankOrNull(address.getAddress()) ||
                !GenericValidator.isEmail(address.getAddress())) {
            throw new InvalidAddessException("Recipient address must be a valid email address");
        }
    }

    private Session getSession(Config config) {
        javax.mail.Authenticator authenticator = null;
        if ((Boolean)config.getOrDefault("auth", false)) {
            authenticator = new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication((String)config.getOrDefault("username", ""), (String)config.getOrDefault("password", ""));
                }
            };
        }
        Properties props = getProperties(config);
        Session session = Session.getInstance(props, authenticator);
        return session;
    }

    private Properties getProperties(Config config) {
        Properties props = new Properties();

        props.put("mail.smtp.host", config.getOrDefault("smtpServer", ""));
        props.put("mail.smtp.port", String.valueOf(config.getOrDefault("port", 25)));
        props.put("mail.smtp.auth", String.valueOf(config.getOrDefault("auth", false)));

        if ("TLS".equals(config.getOrDefault("security", ""))) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if ("SSL".equals(config.getOrDefault("security", ""))) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(config.getOrDefault("port", 25)));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        return props;
    }

    private MimeMessage createMimeMessage(Session session, Address sender) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender.getAddress(), sender.getName()));
        return message;
    }

    private MimeMultipart prepareMessage(Config config, MimeMessage message, Address recipient, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        String contentType = config.getOrDefault("contentType", "") + "; charset=\"utf-8\"";

        if (GenericValidator.isBlankOrNull(recipient.getName())) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getAddress()));
        } else {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getAddress(), recipient.getName()));
        }

        message.setHeader("Content-Type", contentType);
        message.setSubject(subject, "UTF-8");

        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setHeader("Content-Type", contentType);
        messageBodyPart.setContent(content, contentType);
        multipart.addBodyPart(messageBodyPart);

        return multipart;
    }

    private void appendAttachments(MimeMultipart multipart, List<Attachment> attachments) throws MessagingException {
        for (Attachment attachment : attachments) {
            appendAttachment(multipart, attachment);
        }
    }

    private void appendAttachment(MimeMultipart multipart, Attachment attachment) throws MessagingException {
        MimeBodyPart messageBodyPart = new PreencodedMimeBodyPart("base64");
        if (!GenericValidator.isBlankOrNull(attachment.getContentType())) {
            messageBodyPart.setHeader("Content-Type", attachment.getContentType());
        }
        messageBodyPart.setFileName(attachment.getFilename());
        messageBodyPart.setText(attachment.getPayload());
        multipart.addBodyPart(messageBodyPart);
    }

    private void sendMail(Config config, Session session, MimeMessage message) throws MessagingException, UnknownHostException {
        if ((Boolean)config.getOrDefault("simulate", false)) {
            return;
        }

        Transport tr = session.getTransport("smtp");
        connectTransport(tr, config);
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

    private void connectTransport(Transport tr, Config config) throws MessagingException, UnknownHostException {
        if (!(Boolean)config.getOrDefault("auth", false)) {
            tr.connect(
                    (String)config.getOrDefault("smtpServer", ""),
                    (Integer)config.getOrDefault("port", 25),
                    null,
                    null
            );
        } else {
            tr.connect(
                    (String)config.getOrDefault("smtpServer", ""),
                    (Integer)config.getOrDefault("port", 25),
                    (String)config.getOrDefault("username", ""),
                    (String)config.getOrDefault("password", "")
            );
        }
    }
}
