package com.raje.sarma.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.StringUtils;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "logger")
public class Logger implements Serializable {

  @Serial
  private static final long serialVersionUID = 965533642391209431L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String requestUri;
  private String httpMethod;
  private String request;
  @Lob
  private String response;
//  private Map<String, String> headers;

  @Lob
  private String errorMessage;

  @Lob
  private String stackTrace;
  private Long apiDuration;
  @CreatedBy
  private String createdBy;
  @LastModifiedBy
  private String lastUpdatedBy;
  @CreatedDate
  private LocalDateTime createdDate;
  @LastModifiedDate
  private LocalDateTime lastUpdatedDate;

  @PrePersist
  protected void onCreate() {

    if (!StringUtils.hasText(getCreatedBy())) {
      this.createdBy = "LOCALHOST";
    }

    if (getCreatedDate() == null) {
      this.createdDate = LocalDateTime.now();
    }

    if (!StringUtils.hasText(getLastUpdatedBy())) {
      this.lastUpdatedBy = "LOCALHOST";
    }

    if (getLastUpdatedDate() == null) {
      this.lastUpdatedDate = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {

    if (!StringUtils.hasText(getLastUpdatedBy())) {
      this.lastUpdatedBy = "LOCALHOST";
    }

    if (getLastUpdatedDate() == null) {
      this.lastUpdatedDate = LocalDateTime.now();
    }
  }
}
