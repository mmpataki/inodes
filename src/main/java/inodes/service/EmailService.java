package inodes.service;

import inodes.Configuration;
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

    public void sendEmail(Set<String> to, String subject, String body) throws Exception {

        System.out.println("to = " + to + ", subject = " + subject + ", body = " + body);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(conf.getProperty("emailservice.sender.email.id")));
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(to.stream().collect(Collectors.joining(","))));
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);
        Transport.send(message);
    }

}
