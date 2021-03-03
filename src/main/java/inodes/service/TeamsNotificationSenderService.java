package inodes.service;

import com.google.gson.Gson;
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
import javax.servlet.ServletOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamsNotificationSenderService {

    @Autowired
    Configuration conf;

    Session session = null;

    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class TeamsNotification implements NotificationPayLoad {
        String body;
    }

    public void send(String s_url, TeamsNotification not) throws Exception {
        URL url = new URL(s_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.getOutputStream().write(getBytes(not));

        InputStream in = connection.getInputStream();
        byte[] buf = new byte[4096];
        int length;
        while ((length = in.read(buf)) > 0) {
            System.out.write(buf, 0, length);
        }
    }

    private byte[] getBytes(TeamsNotification not) {
        return new Gson().toJson(Collections.singletonMap("text", not.getBody())).getBytes();
    }

}
