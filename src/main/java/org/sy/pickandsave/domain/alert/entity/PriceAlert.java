package org.sy.pickandsave.domain.alert.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.users.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/*
* 알림 설정 엔티티
* */
@Entity
@Table(name = "PRICE_ALERTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PriceAlert {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_alerts_seq_gen")
  @SequenceGenerator(name = "price_alerts_seq_gen", sequenceName = "PRICE_ALERTS_SEQ", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "target_price", nullable = false)
  private Long targetPrice;

  @Column(name = "alert_type", length = 30, nullable = false)
  private String alertType; // e.g. "EMAIL", "WEB_PUSH", "KAKAO"

  @Column(name = "is_active", nullable = false)
  private boolean active; // 오라클 NUMBER(1)와 자동 매핑

  @Column(name = "drop_percent", precision = 5, scale = 2)
  private BigDecimal dropPercent; // 등록 대비 하락률 알림용

  @Column(name = "triggered_at")
  private LocalDateTime triggeredAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
    this.active = true; // 기본적으로 활성화 상태로 등록
  }

  @PreUpdate
  protected void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * 알림이 성공적으로 트리거되어 목표 조건에 도달했을 때 비활성화 처리합니다.
   */
  public void trigger() {
    this.active = false;
    this.triggeredAt = LocalDateTime.now();
  }
}