package org.sy.pickandsave.domain.alert.entity;

import jakarta.persistence.*;
import lombok.*;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.users.entity.User;

import java.time.LocalDateTime;


/*
* 발송 이력 엔티티
* */
@Entity
@Table(name = "NOTIFICATIONS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_seq_gen")
  @SequenceGenerator(name = "notifications_seq_gen", sequenceName = "NOTIFICATIONS_SEQ", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "alert_id")
  private PriceAlert priceAlert;

  @Column(name = "notification_type", length = 30, nullable = false)
  private String notificationType; // "EMAIL", "WEB_PUSH", "KAKAO"

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  @Column(name = "message", length = 1000, nullable = false)
  private String message;

  @Column(name = "status", length = 20, nullable = false)
  private String status; // "PENDING", "SENT", "FAILED"

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void prePersist() {
    this.createdAt = LocalDateTime.now();
    if (this.status == null) this.status = "PENDING";
  }

  public void markAsSent() {
    this.status = "SENT";
    this.sentAt = LocalDateTime.now();
  }

  public void markAsFailed() {
    this.status = "FAILED";
  }
}