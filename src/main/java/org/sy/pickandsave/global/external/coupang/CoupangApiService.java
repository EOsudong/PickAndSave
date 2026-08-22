package org.sy.pickandsave.global.external.coupang;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sy.pickandsave.domain.products.dto.ProductCreateCommand;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.service.ProductCategoryClassifier;
import org.sy.pickandsave.domain.products.service.ProductService;
import org.sy.pickandsave.global.exception.DuplicateProductException;
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
   * 쿠팡에서 키워드로 상품을 검색하고
   * 검색된 상품을 내부 Product 도메인으로 변환하여 저장합니다.
   * <p>
   * 외부 API 통신은 트랜잭션 밖에서 수행하고,
   * 실제 DB 저장은 ProductService가 담당합니다.
   */
  public List<ProductResponse> searchAndSaveProducts(
      String keyword,
      int limit
  ) {

    log.info(
        "[쿠팡 상품 검색 시작] keyword={}, limit={}",
        keyword,
        limit
    );

    // =========================================================
    // 1. 쿠팡 API 호출
    // =========================================================

    CoupangSearchResponse response =
        coupangApiClient.searchProducts(keyword, limit);

    if (response == null) {
      log.warn(
          "[쿠팡 검색 실패] 응답이 null입니다. keyword={}",
          keyword
      );

      return List.of();
    }

    // =========================================================
    // 2. 쿠팡 API 응답 상태 확인
    // =========================================================

    if (!"0".equals(response.getRCode())) {

      String errorMessage = response.getRMessage();

      log.error(
          "[쿠팡 API 오류] keyword={}, rCode={}, message={}",
          keyword,
          response.getRCode(),
          errorMessage
      );

      throw new IllegalStateException(
          "쿠팡 API 오류가 발생했습니다: " + errorMessage
      );
    }

    // =========================================================
    // 3. 검색 결과 데이터 확인
    // =========================================================

    if (response.getData() == null
        || response.getData().getProductData() == null
        || response.getData().getProductData().isEmpty()) {

      log.info(
          "[쿠팡 검색 결과 없음] keyword={}",
          keyword
      );

      return List.of();
    }

    List<CoupangSearchResponse.CoupangItem> items =
        response.getData().getProductData();

    log.info(
        "[쿠팡 검색 완료] keyword={}, 검색상품수={}",
        keyword,
        items.size()
    );

    // =========================================================
    // 4. 쿠팡 상품 → 내부 Product 변환 및 저장
    // =========================================================

    List<ProductResponse> savedProducts = new ArrayList<>();

    for (CoupangSearchResponse.CoupangItem item : items) {

      try {

        // -------------------------------------------------
        // 카테고리 자동 분류
        // -------------------------------------------------

        Long categoryId =
            productCategoryClassifier
                .classify(item.getProductName())
                .orElse(null);

        // -------------------------------------------------
        // 쿠팡 DTO → ProductCreateCommand
        // -------------------------------------------------

        ProductCreateCommand command =
            ProductCreateCommand.builder()
                .coupangProductId(item.getProductId())
                .productName(item.getProductName())
                .coupangProductUrl(item.getProductUrl())

                /*
                 * 현재는 쿠팡 원본 URL 사용.
                 *
                 * 추후 딥링크 API를 붙이면
                 * 이 부분을 생성된 affiliate URL로 변경.
                 */
                .partnersAffiliateUrl(item.getProductUrl())

                .imageUrl(item.getProductImage())
                .categoryId(categoryId)
                .currentPrice(item.getProductPrice())
                .rocket(item.isRocket())
                .build();

        // -------------------------------------------------
        // ProductService에서 실제 DB 저장
        // -------------------------------------------------

        ProductResponse savedProduct =
            productService.saveOrUpdateCoupangProduct(command);

        savedProducts.add(savedProduct);

        log.info(
            "[쿠팡 상품 저장 성공] coupangProductId={}, productName={}",
            item.getProductId(),
            item.getProductName()
        );

      } catch (DuplicateProductException e) {

        /*
         * 이미 DB에 존재하는 상품.
         *
         * 현재 검색 API의 역할은 '신규 상품 수집'이므로
         * 중복 상품은 저장하지 않고 넘어감.
         */
        log.info(
            "[쿠팡 상품 중복] coupangProductId={}, 상품명={}",
            item.getProductId(),
            item.getProductName()
        );

      } catch (Exception e) {

        /*
         * 특정 상품 하나의 저장 실패 때문에
         * 검색 결과 전체를 실패시키지 않음.
         */
        log.error(
            "[쿠팡 상품 저장 실패] coupangProductId={}, 상품명={}, error={}",
            item.getProductId(),
            item.getProductName(),
            e.getMessage(),
            e
        );
      }
    }

    log.info(
        "[쿠팡 상품 검색 및 저장 완료] keyword={}, 검색={}건, 신규저장={}건",
        keyword,
        items.size(),
        savedProducts.size()
    );

    return savedProducts;
  }
}