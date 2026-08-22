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
import java.util.Optional;

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

  /**
   * 최신 가격 이력을 검증 후 안전하게 적재하고, PRODUCTS 엔티티 통계 연산을 가동시킵니다.
   */
  @Transactional
  public void recordPriceAndCalculateStats(Product product, Long newPrice, String source) {
    // 1. 가격 변동 확인 (이전 마지막 이력과 가격이 동일하면 이력 추가 건너뜀)
    Optional<PriceHistory> latestHistoryOpt =
        priceHistoryRepository.findFirstByProductIdOrderByRecordedAtDesc(product.getId());

    if (latestHistoryOpt.isPresent() && latestHistoryOpt.get().getPrice().equals(newPrice)) {
      log.info("상품 가격 변동 없음 - ID: {}, 가격: {}원 (수집 제외 후 확인시간 동기화)",
          product.getId(), newPrice);
      // 가격은 변하지 않았으나 확인 일자는 주기적으로 업데이트
      product.updateLastCheckedAt();
      return;
    }

    // 2. 변동이 감지되었거나 최초 이력 등록인 경우에만 인서트
    log.info("새로운 가격 감지 - ID: {}, {}원 ➔ {}원. 가격 이력(PriceHistory)을 영속화합니다.",
        product.getId(), latestHistoryOpt.map(PriceHistory::getPrice).orElse(0L), newPrice);

    PriceHistory priceHistory = PriceHistory.builder()
        .product(product)
        .price(newPrice)
        .source(source)
        .recordedAt(LocalDateTime.now())
        .build();
    priceHistoryRepository.save(priceHistory);

    // 3. 통계 연산을 위한 전체 합계 및 건수 재조회
    long totalPriceSum = priceHistoryRepository.sumPriceByProductId(product.getId());
    long totalCount = priceHistoryRepository.countByProductId(product.getId());

    if (totalPriceSum == -1) totalPriceSum = newPrice;
    if (totalCount == -1 || totalCount == 0) totalCount = 1L;


    // 4. 메인 상품의 최저/최고/평균 가격 및 현재가 반영 업데이트
    product.updatePrice(newPrice, totalPriceSum, totalCount);
    log.info("[이력 갱신] 신규 가격 반영 완료. 상품 ID: {}, 신규 가격: {}", product.getId(), newPrice);
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