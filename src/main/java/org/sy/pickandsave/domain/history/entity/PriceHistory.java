package org.sy.pickandsave.domain.history.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sy.pickandsave.domain.products.entity.Product;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRICE_HISTORIES", indexes = {
    // 조회 및 차트 출력용 정렬 기준에 맞춰 recorded_at 복합 인덱스로 교체
    @Index(name = "idx_price_history_product_date", columnList = "product_id, recorded_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_history_seq_gen")
  @SequenceGenerator(name = "price_history_seq_gen", sequenceName = "PRICE_HISTORIES_SEQ", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "price", nullable = false)
  private Long price;

  @Column(name = "source", length = 20, nullable = false)
  private String source;

  @Column(name = "recorded_at", nullable = false)
  private LocalDateTime recordedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public PriceHistory(
      Product product,
      Long price,
      String source,
      LocalDateTime recordedAt
  ) {
    this.product = product;
    this.price = price;
    this.source = source != null ? source : "COUPANG";
    this.recordedAt = recordedAt;
  }

  @PrePersist
  protected void prePersist() {
    LocalDateTime now = LocalDateTime.now();

    if (recordedAt == null) {
      recordedAt = now;
    }

    if (createdAt == null) {
      createdAt = now;
    }
  }
}