package inodes.service.api;

import inodes.models.Klass;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j
@Service
public abstract class KlassService {

    @Autowired
    AuthorizationService AS;

    public abstract Klass getKlass(String name) throws Exception;

    public abstract List<Klass> getAllKlasses() throws Exception;

    public void putKlass(Klass klass) throws Exception {
        AS.checkKlassCreatePermission();
        _putKlass(klass);
    }

    public abstract void _putKlass(Klass klass) throws Exception;

    public abstract List<String> getRegisteredKlasses() throws Exception;
}
