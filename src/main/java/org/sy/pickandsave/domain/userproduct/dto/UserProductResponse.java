package org.sy.pickandsave.domain.userproduct.dto;

import lombok.Builder;
import lombok.Getter;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.userproduct.entity.UserProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserProductResponse {
  private Long id;

  private Long productId;

  private Long coupangProductId;

  private String productName;

  private String imageUrl;

  private BigDecimal currentPrice;

  private BigDecimal lowestPrice;

  private BigDecimal highestPrice;

  private BigDecimal averagePrice;

  private String productUrl;

  private LocalDateTime savedAt;

  public static UserProductResponse from(UserProduct userProduct) {

    Product product = userProduct.getProduct();

    return UserProductResponse.builder()
        .id(userProduct.getId())
        .productId(product.getId())
        .coupangProductId(product.getCoupangProductId())
        .productName(product.getProductName())
        .imageUrl(product.getImageUrl())
        .currentPrice(BigDecimal.valueOf(product.getCurrentPrice()))
        .lowestPrice(BigDecimal.valueOf(product.getLowestPrice()))
        .highestPrice(BigDecimal.valueOf(product.getHighestPrice()))
        .averagePrice(product.getAveragePrice())
        .productUrl(product.getCoupangProductUrl())
        .savedAt(userProduct.getCreatedAt())
        .build();
  }
}