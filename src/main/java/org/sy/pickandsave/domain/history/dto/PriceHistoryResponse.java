package org.sy.pickandsave.domain.history.dto;

import lombok.Builder;
import lombok.Getter;
import org.sy.pickandsave.domain.history.entity.PriceHistory;

import java.time.LocalDateTime;

@Getter
@Builder
public class PriceHistoryResponse {
  private Long price;
  private LocalDateTime recordedAt;

  public static PriceHistoryResponse from(PriceHistory history) {
    return PriceHistoryResponse.builder()
        .price(history.getPrice())
        .recordedAt(history.getCreatedAt())
        .build();
  }
}