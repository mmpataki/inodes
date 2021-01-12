package inodes.service;

import inodes.service.api.AuthenticationService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileBasedAuthenticationService implements AuthenticationService {

    Map<String, Credentials> users = new HashMap<>();
    PrintWriter pw;
    String file = "./users.txt";

    @PostConstruct
    void init() throws Exception {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null) {
                Credentials cr = makeCredential(line);
                users.put(cr.getUser(), cr);
            }
        } catch (FileNotFoundException e) {
            // nothing, it's fine
        }
        pw = new PrintWriter(new FileWriter(file, true));
    }

    private Credentials makeCredential(String line) {
        String chunks[] = line.split(":");
        return new Credentials(chunks[0], chunks[1], chunks[2], Boolean.parseBoolean(chunks[3]));
    }

    @Override
    public boolean authenticate(Credentials cred) {
        Credentials c = users.get(cred.getUser());
        if(c != null) {
            return c.isVerified() && c.getPassword().equals(cred.getPassword());
        }
        return false;
    }

    @Override
    public String register(Credentials cred) {
        if(users.containsKey(cred.getUser())) {
            return "exists";
        }
        pw.println(makeCredentialLine(cred));
        pw.flush();
        users.put(cred.getUser(), cred);
        return "success";
    }

    @Override
    public Credentials getUser(String userName) {
        return users.get(userName);
    }

    @Override
    public boolean isAdmin(String userId) {
        return false;
    }

    private String makeCredentialLine(Credentials cred) {
        return String.format("%s:%s:%s:%s", cred.getUser(), cred.getPassword(), cred.getRoles(), cred.isVerified());
    }
}
