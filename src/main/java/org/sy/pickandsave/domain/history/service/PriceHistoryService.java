package org.sy.pickandsave.domain.history.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.history.dto.PriceHistoryResponse;
import org.sy.pickandsave.domain.history.entity.PriceHistory;
import org.sy.pickandsave.domain.history.repository.PriceHistoryRepository;
import org.sy.pickandsave.domain.products.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceHistoryService {

  private final PriceHistoryRepository priceHistoryRepository;

  /**
   * 가격 이력을 기록하고, 해당 상품의 가격 통계(최저/최고/평균가)를 업데이트합니다.
   */
  @Transactional
  public void recordPriceAndCalculateStats(Product product, Long newPrice) {
    recordPriceAndCalculateStats(product, newPrice, "COUPANG");
  }

  @Transactional
  public void recordPriceAndCalculateStats(Product product, Long newPrice, String source) {
    // 1. 가격 이력 저장
    PriceHistory history = PriceHistory.builder()
        .product(product)
        .price(newPrice)
        .source(source)
        .recordedAt(LocalDateTime.now())
        .build();

    priceHistoryRepository.save(history);

    // 2. 전체 이력 기반 가격 통계 재계산 및 Product Entity 갱신
    Long totalPriceSum = priceHistoryRepository.sumPriceByProductId(product.getId());
    Long totalCount = priceHistoryRepository.countByProductId(product.getId());

    if (totalPriceSum != null && totalCount != null && totalCount > 0) {
      product.updatePrice(newPrice, totalPriceSum, totalCount);
    }
  }

  /**
   * 특정 상품의 가격 히스토리(시간순 오름차순) 조회
   */
  public List<PriceHistoryResponse> getPriceHistory(Long productId) {
    return priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(productId).stream()
        .map(PriceHistoryResponse::from)
        .toList();
  }
}