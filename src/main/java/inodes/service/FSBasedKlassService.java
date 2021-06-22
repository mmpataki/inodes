package inodes.service;

import com.google.gson.Gson;
import inodes.models.Klass;
import inodes.service.api.KlassService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j
public class FSBasedKlassService extends KlassService {

    String basePath = "./klasses";

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(basePath));
    }

    String makePath(String kName, boolean createIfNotExist) throws IOException {
        String ret = basePath + "/" + kName;
        if(createIfNotExist)
            Files.createDirectories(Paths.get(ret));
        return ret;
    }

    public List<String> getRegisteredKlasses() throws Exception {
        return Arrays.asList(new File(basePath).list());
    }

    @Override
    public Klass getKlass(String name) throws Exception {
        try (FileReader fr = new FileReader(makePath(name, false) + "/class.json")) {
            return new Gson().fromJson(fr, Klass.class);
        }
    }

    @Override
    public List<Klass> getAllKlasses() throws Exception {
        return getRegisteredKlasses().stream().map(k -> {
            try {
                return getKlass(k);
            } catch (Exception e) {
                log.error("error while loading " + k, e);
                return null;
            }
        }).filter(x -> x != null).collect(Collectors.toList());
    }

    @Override
    public void _putKlass(Klass klass) throws Exception {
        try (FileWriter fw = new FileWriter(makePath(klass.getName(), true)+ "/class.json")) {
            new Gson().toJson(klass, fw);
        }
    }
}
