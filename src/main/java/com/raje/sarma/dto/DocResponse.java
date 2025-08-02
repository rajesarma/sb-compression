package com.raje.sarma.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 1957009339625590724L;

  private byte[] document;
  private Long docId;
  private String docName;
  private String ruleCategory;
  private String createdBy;
  private String lastUpdatedBy;
  private LocalDateTime createdDate;
  private LocalDateTime lastUpdatedDate;
}
