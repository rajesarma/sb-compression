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
import java.sql.Timestamp;
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
@Entity(name = "doc_data")
public class DocData implements Serializable {

  @Serial
  private static final long serialVersionUID = 4923125325036122726L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String documentName;
  @Lob
  private byte[] document;
  private Boolean active;

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
