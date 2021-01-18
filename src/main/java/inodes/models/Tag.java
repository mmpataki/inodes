package inodes.models;

public class Tag {
    String name;
    String desc;
    long hits;

    public Tag(String name, String desc, long hits) {
        this.name = name;
        this.desc = desc;
        this.hits = hits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }
}
