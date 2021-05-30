package inodes.controllers;

import inodes.models.Klass;
import inodes.service.api.Observable;
import inodes.service.api.KlassService;
import inodes.service.api.UnAuthorizedException;
import inodes.util.SecurityUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.util.List;

@RestController
@CrossOrigin
@Log4j
public class KlassController extends WebMvcConfigurerAdapter {

    @Autowired
    KlassService KS;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String plugDir = System.getenv("PLUGINS_DIR");
        String arg[] = {"file:./src/main/resources/static"};
        if (plugDir != null && !plugDir.isEmpty() && new File(plugDir).exists()) {
            arg = new String[]{"file:./src/main/resources/static", "file:" + plugDir};
        } else {
            log.warn("PLUGINS_DIR environment variable is not set, user plugins won't be available");
        }
        registry.addResourceHandler("/p/**").addResourceLocations(arg);
    }

    @GetMapping("/klass/{id}")
    public Klass get(@PathVariable String id) throws Exception {
        return KS.getKlass(id);
    }

    @PostMapping("/klass")
    public void register(@RequestBody Klass klass) throws Exception {
        KS.putKlass(klass);
    }

    @GetMapping(value = "/klasses")
    public List<String> getRegisteredKlasses() throws Exception {
        return KS.getRegisteredKlasses();
    }
}
