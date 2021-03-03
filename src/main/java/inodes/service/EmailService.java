package inodes.service;

import inodes.Configuration;
import inodes.service.api.NotificationPayLoad;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    Configuration conf;

    Session session = null;

    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class EmailObject implements NotificationPayLoad {
        String subject;
        String body;
    }

    @PostConstruct
    public void init() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", Boolean.parseBoolean(conf.getProperty("emailservice.auth.enabled")));
        prop.put("mail.smtp.starttls.enable", Boolean.parseBoolean(conf.getProperty("emailservice.ssl.enabled")));
        prop.put("mail.smtp.host", conf.getProperty("emailservice.smtp.host"));
        prop.put("mail.smtp.port", conf.getProperty("emailservice.smtp.port"));
        prop.put("mail.smtp.ssl.trust", conf.getProperty("emailservice.smtp.trust"));
        session = Session.getInstance(prop);
    }

    public void send(String to, EmailObject eo) throws Exception {

        System.out.println("to = " + to + ", subject = " + eo.getSubject() + ", body = " + eo.getBody());

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(conf.getProperty("emailservice.sender.email.id")));
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(to));
        message.setSubject(eo.getSubject());

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(eo.getBody(), "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);
        Transport.send(message);
    }

}
