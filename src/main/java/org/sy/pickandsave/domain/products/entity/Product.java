package org.sy.pickandsave.domain.products.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTS", indexes = {
		@Index(
				name = "idx_products_last_checked",
				columnList = "last_checked_at"
		)})
@Getter
@NoArgsConstructor
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 쿠팡 상품 ID
	 */
	@Column(
			name = "coupang_product_id",
			nullable = false,
			unique = true
	)
	private Long coupangProductId;

	/**
	 * 상품명
	 */
	@Column(
			name = "product_name",
			length = 500,
			nullable = false
	)
	private String productName;

	/**
	 * 쿠팡 상품 원본 URL
	 */
	@Column(
			name = "coupang_product_url",
			length = 2000,
			nullable = false
	)
	private String coupangProductUrl;

	/**
	 * 쿠팡 파트너스 제휴 URL
	 * <p>
	 * /deeplink API를 통해 생성
	 */
	@Column(
			name = "partners_affiliate_url",
			length = 2000
	)
	private String partnersAffiliateUrl;

	/**
	 * 상품 이미지 URL
	 */
	@Column(
			name = "image_url",
			length = 2000
	)
	private String imageUrl;

	/**
	 * 상품 카테고리
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private ProductCategory category;

	/**
	 * 현재 가격
	 */
	@Column(
			name = "current_price",
			nullable = false
	)
	private Long currentPrice;

	/**
	 * 역대 최저가
	 */
	@Column(
			name = "lowest_price",
			nullable = false
	)
	private Long lowestPrice;

	/**
	 * 역대 최고가
	 */
	@Column(
			name = "highest_price",
			nullable = false
	)
	private Long highestPrice;

	/**
	 * 평균 가격
	 */
	@Column(
			name = "average_price",
			precision = 12,
			scale = 2,
			nullable = false
	)
	private BigDecimal averagePrice;

	/**
	 * 로켓배송 여부
	 * <p>
	 * Oracle NUMBER(1)
	 * 1 = true
	 * 0 = false
	 */
	@Column(name = "is_rocket", nullable = false)
	private boolean rocket;

	/**
	 * 마지막 가격 조회 시간
	 */
	@Column(name = "last_checked_at")
	private LocalDateTime lastCheckedAt;

	/**
	 * 생성일
	 */
	@Column(
			name = "created_at",
			nullable = false,
			updatable = false
	)
	private LocalDateTime createdAt;

	/**
	 * 수정일
	 */
	@Column(
			name = "updated_at",
			nullable = false
	)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void prePersist() {
		LocalDateTime now = LocalDateTime.now();

		if (createdAt == null) {
			createdAt = now;
		}

		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	protected void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}