package inodes.service.api;

import com.google.gson.Gson;
import inodes.Configuration;
import inodes.models.Document;
import inodes.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class AdminService {

    @Autowired
    DataService DS;

    @Autowired
    Configuration conf;

    public void iterateAllDocs(Consumer<Document> consumer) throws Exception {
        DS.iterateAllDocs("*", consumer);
    }

    public void deleteAll() throws Exception {
        iterateAllDocs(d -> {
            try {
                DS._deleteObj(d.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadTrusted() throws Exception {
        File dir = new File(conf.getProperty("adminservice.trusted.doc.dir"));
        Gson g = new Gson();
        try {
            SecurityUtil.setCurrentUser(UserGroupService.ADMIN);
            for (File file : dir.listFiles()) {
                Document d = g.fromJson(new FileReader(file), Document.class);
                DS.createContent(d, "restored version : " + new Date());
                DS.approve(d.getId());
            }
        } finally {
            SecurityUtil.unsetCurrentUser();
        }
    }

    public void trust(String id) throws Exception {
        Document d = DS.get(id);
        FileWriter fw = new FileWriter(conf.getProperty("adminservice.trusted.doc.dir") + "/" + d.getType() + "-" + d.getId());
        Gson g = new Gson();
        g.toJson(d, fw);
        fw.close();
    }

    public void backup() throws Exception {
        String bdir = conf.getProperty("adminservice.backup.dir");
        if(!new File(bdir).exists()) {
            Files.createDirectories(Paths.get(bdir));
        }
        iterateAllDocs(d -> {
            try {
                FileWriter fw = new FileWriter(bdir + "/" + d.getType() + "-" + d.getId());
                Gson g = new Gson();
                g.toJson(d, fw);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void restore() throws Exception {
        File dir = new File(conf.getProperty("adminservice.backup.dir"));
        Gson g = new Gson();
        for (File file : dir.listFiles()) {
            Document d = g.fromJson(new FileReader(file), Document.class);
            try {
                SecurityUtil.setCurrentUser(d.getOwner());
                DS.createContent(d, "restored version : " + new Date());
            } finally {
                SecurityUtil.unsetCurrentUser();
            }
        }
    }
}
