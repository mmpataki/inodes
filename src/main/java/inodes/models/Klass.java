package inodes.models;

import java.util.List;

public class Klass {
    String name;
    List<String> jsPaths;
    List<String> cssPaths;
    Integer version;

    public List<String> getJsPaths() {
        return jsPaths;
    }

    public void setJsPaths(List<String> jsPaths) {
        this.jsPaths = jsPaths;
    }

    public List<String> getCssPaths() {
        return cssPaths;
    }

    public void setCssPaths(List<String> cssPaths) {
        this.cssPaths = cssPaths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
