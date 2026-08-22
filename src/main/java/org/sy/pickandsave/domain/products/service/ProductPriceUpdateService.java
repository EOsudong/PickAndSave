package org.sy.pickandsave.domain.products.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.alert.service.PriceAlertService;
import org.sy.pickandsave.domain.history.service.PriceHistoryService;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.products.repository.ProductRepository;
import org.sy.pickandsave.global.external.coupang.CoupangApiClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductPriceUpdateService {

  private final ProductRepository productRepository;
  private final CoupangApiClient coupangApiClient;
  private final PriceHistoryService priceHistoryService;
  private final PriceAlertService priceAlertService;

  /**
   * 시스템 전체 상품에 대한 최신 가격 자동 업데이트 배치를 실행합니다.
   */
  public void updateAllPrices() {
    // default_batch_fetch_size: 100 적용으로 연관관계 Lazy 로딩 시 N+1 쿼리 에러 방지
    List<Product> products = productRepository.findAll();
    log.info("정기 가격 자동 업데이트 배치 작업 시작 - 대상 상품 수: {}개", products.size());

    int successCount = 0;

    for (Product product : products) {
      try {
        boolean isUpdated = updateProductPrice(product);
        if (isUpdated) successCount++;
      } catch (Exception e) {
        log.error("상품 정기 가격 업데이트 도중 예외 발생 - 상품ID: {}, 상품명: {}, 오류: {}",
            product.getId(), product.getProductName(), e.getMessage());
      }
    }
    log.info(" 정기 가격 자동 업데이트 배치 작업 완료 - 성공: {}/{}개", successCount, products.size());
  }

  /**
   * 단건 상품에 대한 실시간 최신가를 조회하여 가격을 비교하고, 조건에 따라 히스토리 등록 및 알림 검사를 수행합니다.
   *
   * @return boolean 가격 갱신 및 처리 완료 여부
   */
  @Transactional
  public boolean updateProductPrice(Product product) {
    // 1. 외부 API 실시간 가격 검색
    Long latestPrice = coupangApiClient.fetchCurrentPrice(product.getCoupangProductId(), product.getProductName());

    // 2. 가격 변동 여부 판단
    if (!product.isPriceChanged(latestPrice)) {
      log.info("[가격 변동 포착] {}원 => {}원 - 상품명: {}",
          product.getCurrentPrice(),
          latestPrice,
          product.getProductName());

      // 가격 히스토리 저장 및 누적 평균 통계 재계산
      priceHistoryService.recordPriceAndCalculateStats(product, latestPrice, "COUPANG");

      // 사용자가 등록한 활성 목표 알림(PRICE_ALERTS) 및 발송 예약(NOTIFICATIONS) 검사 트리거
      priceAlertService.checkAndTriggerAlerts(product, latestPrice);

      // 마지막 확인 날짜 갱신
      product.updateLastCheckedAt();
      return true;
    } else {
      // 변동 NO 마지막 확인 날짜만 갱신 후 종료 (DB 부하 예방)
      product.updateLastCheckedAt();
      log.info("[가격 변동 없음] ID: {} | 상품명: {} | 현재가 유지: {}원",
          product.getId(), product.getProductName(), latestPrice);
      return false;
    }
  }

}//class