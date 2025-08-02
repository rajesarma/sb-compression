package com.raje.sarma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocRequest {

  @NotNull(message = "Document data can not be empty or null")
  private MultipartFile document;

  @NotNull(message = "Document file name can not be empty or null")
  private String documentName;

}
