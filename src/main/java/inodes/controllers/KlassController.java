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
public class KlassController {

    @Autowired
    KlassService KS;

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
