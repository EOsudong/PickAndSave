package org.sy.pickandsave.domain.products.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
  private final ProductRepository productRepository;
  private final ProductCategoryRepository categoryRepository;
  private final PriceHistoryService priceHistoryService;

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

}
