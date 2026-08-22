package org.sy.pickandsave.domain.history.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sy.pickandsave.domain.history.dto.PriceHistoryResponse;
import org.sy.pickandsave.domain.history.service.PriceHistoryService;
import org.sy.pickandsave.global.common.ApiResponse;

import java.util.List;

@Tag(name = "PriceHistory", description = "상품 가격 이력 API")
@RestController
@RequestMapping("/api/price-histories")
@RequiredArgsConstructor
public class PriceHistoryController {

  private final PriceHistoryService priceHistoryService;

  @Operation(summary = "상품 가격 이력 조회", description = "상품 ID(productId)를 받아 가격 변동 히스토리를 시간순으로 조회합니다.")
  @GetMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<List<PriceHistoryResponse>>> getPriceHistoriesByProductId(
      @PathVariable("productId") Long productId) {

    List<PriceHistoryResponse> response = priceHistoryService.getPriceHistory(productId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}