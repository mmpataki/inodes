package inodes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class})
public class Inodes {
    public static void main(String[] args) {
        SpringApplication.run(Inodes.class, args);
    }
}