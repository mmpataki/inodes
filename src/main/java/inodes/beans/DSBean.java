package inodes.beans;

import inodes.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DSBean {

    @Autowired
    Configuration conf;

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(conf.getProperty("db.driver.class"));
        dataSourceBuilder.url(conf.getProperty("db.url"));
        dataSourceBuilder.username(conf.getProperty("db.user"));
        dataSourceBuilder.password("db.password");
        return dataSourceBuilder.build();
    }

}
