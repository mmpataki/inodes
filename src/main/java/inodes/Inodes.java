package inodes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class})
public class Inodes implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ApplicationContext applicationContext;

    static String localAddr = "http://localhost:8080/";

    public static void main(String[] args) {
        SpringApplication.run(Inodes.class, args);
    }

    public static String getLocalAddr() {
        return localAddr;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            int port = applicationContext.getBean(Environment.class).getProperty("server.port", Integer.class, 8080);
            localAddr = InetAddress.getLocalHost().getHostName() + ":" + port;
            System.out.println(localAddr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}