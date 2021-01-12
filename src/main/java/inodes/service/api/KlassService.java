package inodes.service.api;

import inodes.models.Klass;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface KlassService {

    public Klass getKlass(String name) throws Exception;

    public void putKlass(Klass klass) throws Exception;

    public List<String> getRegisteredKlasses() throws Exception;
}
