package org.sy.pickandsave.domain.history.dto;

import lombok.Builder;
import lombok.Getter;
import org.sy.pickandsave.domain.history.entity.PriceHistory;

import java.time.LocalDateTime;

@Getter
@Builder
public class PriceHistoryResponse {
  private Long id;
  private Long price;
  private String source;
  private LocalDateTime recordedAt;
  private LocalDateTime createdAt;

  public static PriceHistoryResponse from(PriceHistory history) {
    return PriceHistoryResponse.builder()
        .id(history.getId())
        .price(history.getPrice())
        .source(history.getSource())
        .recordedAt(history.getRecordedAt())
        .createdAt(history.getCreatedAt())
        .build();
  }
}