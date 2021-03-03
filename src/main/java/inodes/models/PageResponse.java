package inodes.models;

import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.List;

@Data
@With
@Builder
public class PageResponse<T> {

    List<T> items;

    int pageSize;

    long offset, totalItems;

}
