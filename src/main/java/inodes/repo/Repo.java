package inodes.repo;

import java.util.Set;

public interface Repo<T> {

    void saveObj(T obj);

    T getObj(Set<String> id);

}
