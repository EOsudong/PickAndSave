package org.sy.pickandsave.domain.products.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.history.entity.PriceHistory;
import org.sy.pickandsave.domain.history.repository.PriceHistoryRepository;
import org.sy.pickandsave.domain.history.service.PriceHistoryService;
import org.sy.pickandsave.domain.products.dto.ProductCreateCommand;
import org.sy.pickandsave.domain.products.dto.ProductCreateRequest;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.products.entity.ProductCategory;
import org.sy.pickandsave.domain.products.repository.ProductCategoryRepository;
import org.sy.pickandsave.domain.products.repository.ProductRepository;
import org.sy.pickandsave.global.exception.DuplicateProductException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
  private final ProductRepository productRepository;
  private final ProductCategoryRepository categoryRepository;
  private final PriceHistoryService priceHistoryService;
  private final ProductCategoryClassifier productCategoryClassifier;

  /**
   * HTTP 요청 DTO 기반 등록 (Controller용)
   */
  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    ProductCreateCommand command = ProductCreateCommand.builder()
        .coupangProductId(request.getCoupangProductId())
        .productName(request.getProductName())
        .coupangProductUrl(request.getCoupangProductUrl())
        .partnersAffiliateUrl(request.getPartnersAffiliateUrl())
        .imageUrl(request.getImageUrl())
        .categoryId(request.getCategoryId())
        .currentPrice(request.getCurrentPrice())
        .rocket(request.isRocket())
        .build();

    return createProductByCommand(command);
  }

  /**
   * [흐름의 최종 도달지] Command를 받아 DB에 완벽히 정합성을 조율하며 저장(Upsert)합니다.
   */
  @Transactional
  public ProductResponse saveOrUpdateCoupangProduct(ProductCreateCommand command) {
    Optional<Product> existingProductOpt = productRepository.findByCoupangProductId(command.coupangProductId());

    if (existingProductOpt.isEmpty()) {
      // 최초 신규 등록
      log.info("신규 상품 최초 영속화 진입 - ID: {}", command.coupangProductId());

      // 1. 카테고리 식별 자동 처리
      ProductCategory category = null;
      Optional<Long> categoryIdOpt = productCategoryClassifier.classify(command.productName());
      if (categoryIdOpt.isPresent()) {
        category = categoryRepository.findById(categoryIdOpt.get()).orElse(null);
      }

      // 2. PRODUCT 도메인 저장
      Product newProduct = Product.builder()
          .coupangProductId(command.coupangProductId())
          .productName(command.productName())
          .coupangProductUrl(command.coupangProductUrl())
          .partnersAffiliateUrl(command.partnersAffiliateUrl())
          .imageUrl(command.imageUrl())
          .category(category)
          .currentPrice(command.currentPrice())
          .lowestPrice(command.currentPrice())
          .highestPrice(command.currentPrice())
          .averagePrice(BigDecimal.valueOf(command.currentPrice()))
          .rocket(command.rocket())
          .build();

      Product savedProduct = productRepository.save(newProduct);

      // 3. PRICE_HISTORIES 최초 기록 적재 연동
      priceHistoryService.recordPriceAndCalculateStats(savedProduct, command.currentPrice(), "COUPANG");

      return ProductResponse.from(savedProduct);
    } else {
      // 중복 상품 재유입 => 통계 및 가격 변동 갱신
      Product existingProduct = existingProductOpt.get();
      log.info("기존 등록 상품 확인 => 가격 정합성 검사 진입 - ID: {}", existingProduct.getCoupangProductId());

      if (existingProduct.isPriceChanged(command.currentPrice())) {
        log.info("가격 변동 감지 ({}원 ➔ {}원) => 이력 및 통계 변동 계산", existingProduct.getCurrentPrice(), command.currentPrice());
        priceHistoryService.recordPriceAndCalculateStats(existingProduct, command.currentPrice(), "COUPANG");
      } else {
        log.info("이전 가격과 동일 => DB 팽창 방지를 위해 이력 추가 없이 마지막 검증 시간만 업데이트");
        existingProduct.updateLastCheckedAt();
      }

      return ProductResponse.from(existingProduct);
    }
  }

  /**
   * 내부 Command 기반 등록 (CoupangApiService 및 서비스 간 내부 연동용)
   */
  @Transactional
  public ProductResponse createProductByCommand(ProductCreateCommand command) {
    if (productRepository.existsByCoupangProductId(command.coupangProductId())) {
      throw new DuplicateProductException("이미 등록된 쿠팡 상품 ID입니다: " + command.coupangProductId());
    }

    ProductCategory category = null;
    if (command.categoryId() != null) {
      category = categoryRepository.findById(command.categoryId())
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID입니다."));
    }

    Product product = Product.builder()
        .coupangProductId(command.coupangProductId())
        .productName(command.productName())
        .coupangProductUrl(command.coupangProductUrl())
        .partnersAffiliateUrl(command.partnersAffiliateUrl())
        .imageUrl(command.imageUrl())
        .category(category)
        .currentPrice(command.currentPrice())
        .lowestPrice(command.currentPrice())
        .highestPrice(command.currentPrice())
        .averagePrice(BigDecimal.valueOf(command.currentPrice()))
        .rocket(command.rocket())
        .build();

    Product savedProduct = productRepository.save(product);

    // 최초 가격 이력 생성
    priceHistoryService.recordPriceAndCalculateStats(savedProduct, command.currentPrice());

    return ProductResponse.from(savedProduct);
  }

  public ProductResponse getProductById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + id));
    return ProductResponse.from(product);
  }

  public List<ProductResponse> getAllProducts() {
    return productRepository.findAll().stream()
        .map(ProductResponse::from)
        .toList();
  }

  @Transactional
  public ProductResponse updateCategory(Long productId, Long categoryId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + productId));

    ProductCategory category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID입니다."));

    product.updateCategory(category);   // 더티 체킹으로 자동 UPDATE
    return ProductResponse.from(product);
  }

  public List<ProductResponse> getUncategorizedProducts() {
    return productRepository.findByCategoryIsNull().stream()
        .map(ProductResponse::from)
        .toList();
  }

  public List<ProductResponse> searchProducts(String keyword) {

    if (keyword == null || keyword.isBlank()) {
      return List.of();
    }

    return productRepository
        .findByProductNameContainingIgnoreCase(keyword.trim())
        .stream()
        .map(ProductResponse::from)
        .toList();
  }

}
