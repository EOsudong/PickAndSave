package org.sy.pickandsave.global.external.coupang;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.history.repository.PriceHistoryRepository;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.products.repository.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CoupangApiIntegrationTest {

  @Autowired
  private CoupangApiService coupangApiService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private PriceHistoryRepository priceHistoryRepository;

  @Test
  @DisplayName("신규 상품 검색 시 PRODUCTS 테이블 저장과 동시에 최초 PRICE_HISTORIES 이력이 정확하게 적재된다")
  void searchAndSaveNewProductSuccess() {
    // given
    String keyword = "에어팟";
    int limit = 3;

    // when (최초 검색 및 등록 프로세스 구동)
    List<ProductResponse> results = coupangApiService.searchAndSaveProducts(keyword, limit);

    // then
    if (!results.isEmpty()) {
      ProductResponse firstResult = results.get(0);

      // 1. 상품 등록 검증
      Product savedProduct = productRepository.findByCoupangProductId(firstResult.getCoupangProductId())
          .orElseThrow();
      assertThat(savedProduct.getCurrentPrice()).isEqualTo(firstResult.getCurrentPrice());

      // 2. 최초 가격 이력(PriceHistory) 검증
      long historyCount = priceHistoryRepository.countByProductId(savedProduct.getId());
      assertThat(historyCount).isGreaterThanOrEqualTo(1L);
    }
  }

  @Test
  @DisplayName("이미 등록된 동일 상품을 다시 검색하더라도 중복 예외 없이 기존 상품의 가격 정보와 시간 정보만 업데이트된다")
  void duplicateProductUpsertSuccess() {
    // given
    String keyword = "삼성 모니터";

    // 1차 검색 실행 (최초 등록)
    List<ProductResponse> run1 = coupangApiService.searchAndSaveProducts(keyword, 1);
    if (run1.isEmpty()) return; // 검색 결과가 없는 환경인 경우 종료

    Long coupangProductId = run1.get(0).getCoupangProductId();
    Product savedProductBefore = productRepository.findByCoupangProductId(coupangProductId).orElseThrow();
    long initialHistoryCount = priceHistoryRepository.countByProductId(savedProductBefore.getId());

    // when (동일 상품 2차 검색 실행)
    List<ProductResponse> run2 = coupangApiService.searchAndSaveProducts(keyword, 1);

    // then
    Product savedProductAfter = productRepository.findByCoupangProductId(coupangProductId).orElseThrow();
    long postHistoryCount = priceHistoryRepository.countByProductId(savedProductAfter.getId());

    // 중복 데이터로 인한 추가 삽입이 방지되어 상품 엔티티 수는 여전히 1개여야 함
    assertThat(savedProductBefore.getId()).isEqualTo(savedProductAfter.getId());

    // 가격 변동이 없으므로 가격 이력 수도 늘어나지 않고 유지되어야 함
    assertThat(initialHistoryCount).isEqualTo(postHistoryCount);
    assertThat(savedProductAfter.getLastCheckedAt()).isAfterOrEqualTo(savedProductBefore.getLastCheckedAt());
  }
}