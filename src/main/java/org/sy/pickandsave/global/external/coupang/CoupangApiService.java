package org.sy.pickandsave.global.external.coupang;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sy.pickandsave.domain.products.dto.ProductCreateCommand;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.service.ProductCategoryClassifier;
import org.sy.pickandsave.domain.products.service.ProductService;
import org.sy.pickandsave.global.external.coupang.dto.CoupangSearchResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangApiService {

  private final CoupangApiClient coupangApiClient;
  private final ProductService productService;
  private final ProductCategoryClassifier productCategoryClassifier;

  /**
   * 쿠팡에서 키워드로 상품을 검색한 뒤, 검색된 상품들을 DB에 자동 등록합니다.
   */
  // @Transactional 제거: 외부 API 통신 시 DB Connection 점유 방지
  public List<ProductResponse> searchAndSaveProducts(String keyword, int limit) {
    // 1. 외부 HTTP 통신 수행 (DB 트랜잭션 밖에서 실행)
    CoupangSearchResponse response = coupangApiClient.searchProducts(keyword, limit);

    // 2. 쿠팡 API 응답 내부 상태 코드 확인
    if (response == null || !"0".equals(response.getRCode())) {
      String errorMsg = (response != null) ? response.getRMessage() : "응답 데이터 없음";
      throw new IllegalStateException("쿠팡 API 오류가 발생했습니다: " + errorMsg);
    }

    if (response.getData() == null || response.getData().getProductData() == null) {
      return List.of();
    }

    List<ProductResponse> savedProducts = new ArrayList<>();

    // 3. 쿠팡 DTO -> 내부 Command 변환 후 저장
    for (CoupangSearchResponse.CoupangItem item : response.getData().getProductData()) {
      Long inferredCategoryId = productCategoryClassifier.classify(item.getProductName()).orElse(null);
      ProductCreateCommand command = ProductCreateCommand.builder()
          .coupangProductId(item.getProductId())
          .productName(item.getProductName())
          .coupangProductUrl(item.getProductUrl())
          .partnersAffiliateUrl(item.getProductUrl()) // 기본 수집 시 동일 처리
          .imageUrl(item.getProductImage())
          .categoryId(inferredCategoryId)
          .currentPrice(item.getProductPrice())
          .rocket(item.isRocket())
          .build();

      try {
        ProductResponse savedProduct = productService.createProductByCommand(command);
        savedProducts.add(savedProduct);
      } catch (IllegalArgumentException e) {
        log.info("중복 상품 스킵 (Coupang ID: {}): {}", item.getProductId(), e.getMessage());
      }
    }

    return savedProducts;
  }
}