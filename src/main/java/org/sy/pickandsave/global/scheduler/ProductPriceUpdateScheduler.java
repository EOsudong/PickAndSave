/*
package org.sy.pickandsave.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sy.pickandsave.domain.products.service.ProductPriceUpdateService;


@ConditionalOnProperty(
    name = "scheduler.product-price-update.enabled",
    havingValue = "true"
)
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPriceUpdateScheduler {

  private final ProductPriceUpdateService priceUpdateService;

*/
/**
   * [운영 스펙] 매일 새벽 3시(0 0 3 * * *)마다 전체 상품 최신화 작업 구동
   * [개발/테스트 스펙] 필요 시 fixedDelay = 600000 (10분) 등으로 테스트 가능
   *//*


//  @Scheduled(cron = "0 0 3 * * *")
  @Scheduled(fixedDelay = 600000)
  public void runPriceUpdateJob() {
    log.info("▶▶ 백그라운드 상품 가격 자동 갱신 스케줄러 활성화 ◀◀");
    long startTime = System.currentTimeMillis();

    try {
      priceUpdateService.updateAllPrices();
    } catch (Exception e) {
      log.error("가격 자동 갱신 스케줄러 실행 중 치명적인 배치 오류 발생", e);
    }

    long duration = System.currentTimeMillis() - startTime;
    log.info("▶▶ 백그라운드 가격 갱신 배치 완료 (소요시간: {} ms) ◀◀", duration);
  }
}

*/
