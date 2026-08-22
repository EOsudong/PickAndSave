package org.sy.pickandsave.domain.users.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "USERS",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(
            name = "uk_users_provider_provider_id",
            columnNames = {"provider", "provider_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "users_seq_gen"
  )
  @SequenceGenerator(
      name = "users_seq_gen",
      sequenceName = "USERS_SEQ",
      allocationSize = 1
  )
  private Long id;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "nickname", length = 50, nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", length = 20, nullable = false)
  private AuthProvider provider;

  @Column(name = "provider_id")
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", length = 20, nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private UserStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "plan", nullable = false)
  private UserPlan plan;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "last_login_at", nullable = false)
  private LocalDateTime lastLoginAt;

  public User(String email, String nickname, AuthProvider provider, String providerId, UserPlan plan) {
    this.email = email;
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
    this.plan = plan != null ? plan : UserPlan.FREE; // 기본값 FREE
  }

  @PrePersist
  protected void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (this.createdAt == null) {
      this.createdAt = now;
    }
    if (this.updatedAt == null) {
      this.updatedAt = now;
    }
    if (this.lastLoginAt == null) {
      this.lastLoginAt = now;
    }
    if (this.role == null) {
      this.role = UserRole.ROLE_USER;
    }
    if (this.status == null) {
      this.status = UserStatus.ACTIVE;
    }
    if (this.plan == null) {
      this.plan = UserPlan.FREE;
    }
  }

  @Builder
  public User(String email, String nickname, AuthProvider provider,
              String providerId, UserRole role, UserPlan plan) {
    this.email = email;
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
    this.role = role != null ? role : UserRole.ROLE_USER;
    this.plan = plan != null ? plan : UserPlan.FREE;
  }

  @PreUpdate
  protected void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // Factory Methods
  // 로컬 계정 생성
  public static User createLocalUser(
      String email,
      String password,
      String nickname
  ) {
    User user = new User();

    user.email = email;
    user.password = password;
    user.nickname = nickname;
    user.provider = AuthProvider.LOCAL;

    return user;
  }

  // 소셜 계정 생성
  public static User createSocialUser(
      String email,
      String nickname,
      String provider,
      String providerId
  ) {
    User user = new User();

    user.email = email;
    user.nickname = nickname;
    user.provider = AuthProvider.valueOf(provider);
    user.providerId = providerId;
    user.plan = UserPlan.FREE;

    return user;
  }

  // Update Methods
  // 상태 변경
  public void updateStatus(String status) {
    this.status = UserStatus.valueOf(status);
  }

  // 닉네임 변경
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  // 비밀번호 변경
  public void updatePassword(String password) {
    this.password = password;
  }


}