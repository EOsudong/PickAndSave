package org.sy.pickandsave.domain.products.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sy.pickandsave.domain.history.dto.PriceHistoryResponse;
import org.sy.pickandsave.domain.history.service.PriceHistoryService;
import org.sy.pickandsave.domain.products.dto.ProductCategoryUpdateRequest;
import org.sy.pickandsave.domain.products.dto.ProductCreateRequest;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.service.ProductService;
import org.sy.pickandsave.global.common.ApiResponse;
import org.sy.pickandsave.global.external.coupang.CoupangApiService;

import java.util.List;

@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final CoupangApiService coupangApiService;
  private final PriceHistoryService priceHistoryService;

  @Operation(summary = "상품 등록", description = "신규 쿠팡 상품 정보를 DB에 등록합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
    ProductResponse response = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @Operation(summary = "상품 단건 조회", description = "상품 ID(PK)를 이용해 해당 상품의 상세 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable("id") Long id) {
    ProductResponse response = productService.getProductById(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "상품 전체 목록 조회", description = "등록된 전체 상품 리스트를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
    List<ProductResponse> response = productService.getAllProducts();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "쿠팡 상품 검색 및 DB 자동 저장", description = "키워드로 쿠팡 상품을 검색하여 신규 상품인 경우 DB에 즉시 저장합니다.")
  @PostMapping("/search/coupang")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> searchAndSaveCoupangProducts(
      @RequestParam("keyword") String keyword,
      @RequestParam(value = "limit", defaultValue = "10") int limit) {

    List<ProductResponse> responses = coupangApiService.searchAndSaveProducts(keyword, limit);
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @Operation(summary = "상품 가격 이력 조회", description = "상품의 가격 변동 이력을 시간순으로 조회합니다.")
  @GetMapping("/{id}/price-history")
  public ResponseEntity<ApiResponse<List<PriceHistoryResponse>>> getPriceHistory(@PathVariable("id") Long id) {
    List<PriceHistoryResponse> response = priceHistoryService.getPriceHistory(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }


  /*
  * 관리자 인증 체크가 없습니다 나중에 관리자 계정/인증 시스템을 붙일 때
  * 이 두 엔드포인트(/category, /uncategorized)는
  * 반드시 관리자 권한 체크 대상으로 넣어줘야 합니다.
  * */
  @Operation(summary = "상품 카테고리 수동 지정", description = "관리자가 상품의 카테고리를 직접 지정하거나 수정합니다.")
  @PatchMapping("/{id}/category")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProductCategory(
      @PathVariable("id") Long id,
      @Valid @RequestBody ProductCategoryUpdateRequest request) {

    ProductResponse response = productService.updateCategory(id, request.getCategoryId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "미분류 상품 목록 조회", description = "카테고리가 지정되지 않은 상품 목록을 조회합니다.")
  @GetMapping("/uncategorized")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getUncategorizedProducts() {
    List<ProductResponse> response = productService.getUncategorizedProducts();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
