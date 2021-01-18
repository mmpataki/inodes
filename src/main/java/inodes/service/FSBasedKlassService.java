package inodes.service;

import com.google.gson.Gson;
import inodes.models.Klass;
import inodes.service.api.KlassService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class FSBasedKlassService extends KlassService {

    String basePath = "./klasses";

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(basePath));
    }

    String makePath(String kName) throws IOException {
        String ret = basePath + "/" + kName;
        Files.createDirectories(Paths.get(ret));
        return ret;
    }

    public List<String> getRegisteredKlasses() throws Exception {
        return Arrays.asList(new File(basePath).list());
    }

    @Override
    public Klass getKlass(String name) throws Exception {
        try (FileReader fr = new FileReader(makePath(name) + "/class.json")) {
            return new Gson().fromJson(fr, Klass.class);
        }
    }

    @Override
    public void _putKlass(Klass klass) throws Exception {
        try (FileWriter fw = new FileWriter(makePath(klass.getName())+ "/class.json")) {
            new Gson().toJson(klass, fw);
        }
    }
}
