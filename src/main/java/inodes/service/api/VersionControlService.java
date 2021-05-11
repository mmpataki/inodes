package inodes.service.api;

import com.google.gson.Gson;
import inodes.Configuration;
import inodes.models.Document;
import inodes.util.SecurityUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class VersionControlService {

    @Autowired
    DataService DS;

    @Autowired
    Configuration conf;

    String root;

    @PostConstruct
    public void init() {

        Gson G = new Gson();
        root = conf.getProperty("vcservice.storage.dir");

        Interceptor docObserver = ed -> {

            Document doc = (Document) ed.get("doc");
            String user = SecurityUtil.getCurrentUser();
            String changeNote = (String) ed.get("changeNote");

            if(Files.exists(Paths.get(root)))
                Files.createDirectories(Paths.get(root));

            long itime = System.currentTimeMillis();

            String dir = String.format("%s/%s", root, doc.getId() );
            String nFile = String.format("%s/%d_%s", dir, itime, user);
            String cFile = String.format("%s/change_%d_%s", dir, itime, user);

            Files.createDirectories(Paths.get(dir));
            try (FileWriter fw = new FileWriter(nFile)) {
                G.toJson(doc, fw);
            }
            try (FileWriter fw = new FileWriter(cFile)) {
                fw.write(changeNote);
            }
        };
        DS.registerPreEvent(DataService.ObservableEvents.UPDATE, docObserver);
        DS.registerPreEvent(DataService.ObservableEvents.NEW, docObserver);
    }

    public DocHistory getHistoryOf(String id) throws Exception {
        return new DocHistory(id);
    }

    public Document getDocWithVersion(String id, Long time, String user) throws Exception {
        try (FileReader fr = new FileReader(String.format("%s/%s/%d_%s", root, id, time, user))) {
            return new Gson().fromJson(fr, Document.class);
        } catch (FileNotFoundException fnfe) {
            throw new Exception("No version of this doc found");
        }
    }

    @Data
    public class DocEdit {
        long mtime;
        String author;
        String changeNote;
        DocEdit(String fName, String id) throws Exception {
            mtime = Long.parseLong(fName.split("_")[0]);
            author = fName.split("_")[1];
            try (FileReader fr = new FileReader(String.format("%s/%s/change_%s_%s", root, id, mtime, author))) {
                changeNote = new BufferedReader(fr).readLine();
            }
        }
    }

    @Data
    public class DocHistory {
        String id;
        List<DocEdit> edits = new ArrayList<>();

        DocHistory(String id) throws Exception {
            this.id = id;
            for (String fName : new File(root + "/" + id).list((dir, name) -> !name.startsWith("change"))) {
                edits.add(new DocEdit(fName, id));
            }
        }
    }

}
