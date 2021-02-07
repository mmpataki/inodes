package inodes.models;

import lombok.Data;

import java.util.List;

@Data
public class Klass {
    String name;
    List<String> jsPaths;
    List<String> cssPaths;
    Integer version;
    boolean editApprovalNeeded;
}
