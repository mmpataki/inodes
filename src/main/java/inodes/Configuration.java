package inodes;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Component
public class Configuration {

    private Properties props = new Properties();

    public Configuration() throws IOException {
        props.load(new FileInputStream("./config.properties"));
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

}
