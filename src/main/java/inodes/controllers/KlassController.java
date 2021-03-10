package inodes.controllers;

import inodes.models.Klass;
import inodes.service.api.Observable;
import inodes.service.api.KlassService;
import inodes.service.api.UnAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class KlassController extends AuthenticatedController {

    @Autowired
    KlassService KS;

    @GetMapping("/klass/{id}")
    public Klass get(@PathVariable String id) throws Exception {
        return KS.getKlass(id);
    }

    @PostMapping("/klass")
    public void register(@RequestBody Klass klass, @ModelAttribute("loggedinuser") String user) throws Exception {
        KS.putKlass(user, klass);
    }

    @GetMapping(value = "/klasses")
    public List<String> getRegisteredKlasses() throws Exception {
        return KS.getRegisteredKlasses();
    }
}
