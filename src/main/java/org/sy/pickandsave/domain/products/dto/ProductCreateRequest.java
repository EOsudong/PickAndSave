package org.sy.pickandsave.domain.products.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

  @NotNull(message = "쿠팡 상품 ID는 필수입니다.")
  private Long coupangProductId;

  @NotBlank(message = "상품명은 필수입니다.")
  private String productName;

  @NotBlank(message = "쿠팡 상품 URL은 필수입니다.")
  private String coupangProductUrl;

  private String partnersAffiliateUrl;

  private String imageUrl;

  private Long categoryId;

  @NotNull(message = "현재 가격은 필수입니다.")
  private Long currentPrice;

  private boolean rocket;
}
