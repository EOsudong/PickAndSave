package org.sy.pickandsave.global.external.coupang.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CoupangSearchResponse {

  private String rCode;
  private String rMessage;
  private CoupangData data;

  @Getter
  @NoArgsConstructor
  public static class CoupangData {
    private String landingUrl;
    private List<CoupangItem> productData;
  }

  @Getter
  @NoArgsConstructor
  public static class CoupangItem {
    private Long productId;
    private String productName;
    private Long productPrice;
    private String productImage;
    private String productUrl;

    @JsonProperty("isRocket")
    private boolean isRocket;

    @JsonProperty("isFreeShipping")
    private boolean isFreeShipping;
  }
}