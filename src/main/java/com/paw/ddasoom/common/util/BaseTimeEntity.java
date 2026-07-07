package com.paw.ddasoom.common.util;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 이벤트 리스너 활성화
public abstract class BaseTimeEntity {

  @CreatedDate
  @Column(updatable = false, nullable = false,  columnDefinition = "DATETIME(6)")
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false ,  columnDefinition = "DATETIME(6)")
  private LocalDateTime updatedAt;

}
