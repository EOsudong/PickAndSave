package org.sy.pickandsave.domain.products.dto;


import lombok.Builder;
import lombok.Getter;
import org.sy.pickandsave.domain.products.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {
  private Long id;
  private Long coupangProductId;
  private String productName;
  private String coupangProductUrl;
  private String partnersAffiliateUrl;
  private String imageUrl;
  private Long categoryId;
  private Long currentPrice;
  private Long lowestPrice;
  private Long highestPrice;
  private BigDecimal averagePrice;
  private boolean rocket;
  private LocalDateTime lastCheckedAt;
  private LocalDateTime createdAt;

  public static ProductResponse from(Product entity) {
    return ProductResponse.builder()
        .id(entity.getId())
        .coupangProductId(entity.getCoupangProductId())
        .productName(entity.getProductName())
        .coupangProductUrl(entity.getCoupangProductUrl())
        .partnersAffiliateUrl(entity.getPartnersAffiliateUrl())
        .imageUrl(entity.getImageUrl())
        .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
        .currentPrice(entity.getCurrentPrice())
        .lowestPrice(entity.getLowestPrice())
        .highestPrice(entity.getHighestPrice())
        .averagePrice(entity.getAveragePrice())
        .rocket(entity.isRocket())
        .lastCheckedAt(entity.getLastCheckedAt())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}
