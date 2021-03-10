package inodes.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Klass {
    String name;
    List<String> jsPaths;
    List<String> cssPaths;
    Integer version;
    boolean editApprovalNeeded;
}
