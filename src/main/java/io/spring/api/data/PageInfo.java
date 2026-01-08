package io.spring.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
  private boolean hasNextPage;
  private boolean hasPreviousPage;
  private String startCursor;
  private String endCursor;
}
