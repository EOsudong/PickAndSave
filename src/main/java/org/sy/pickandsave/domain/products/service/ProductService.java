package org.sy.pickandsave.domain.products.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.products.dto.ProductCreateRequest;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.products.entity.ProductCategory;
import org.sy.pickandsave.domain.products.repository.ProductCategoryRepository;
import org.sy.pickandsave.domain.products.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Builder
public class ProductService {
  private final ProductRepository productRepository;
  private final ProductCategoryRepository categoryRepository;

  @Transactional
  public ProductResponse createProduct(ProductCreateRequest request) {
    if (productRepository.existsByCoupangProductId(request.getCoupangProductId())) {
      throw new IllegalArgumentException("이미 등록된 쿠팡 상품 ID입니다: " + request.getCoupangProductId());
    }

    ProductCategory category = null;
    if (request.getCategoryId() != null) {
      category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID입니다."));
    }

    // 초기 수집 시 현재가를 최저가/최고가/평균가로 지정
    Product product = Product.builder()
        .coupangProductId(request.getCoupangProductId())
        .productName(request.getProductName())
        .coupangProductUrl(request.getCoupangProductUrl())
        .partnersAffiliateUrl(request.getPartnersAffiliateUrl())
        .imageUrl(request.getImageUrl())
        .category(category)
        .currentPrice(request.getCurrentPrice())
        .lowestPrice(request.getCurrentPrice())
        .highestPrice(request.getCurrentPrice())
        .averagePrice(BigDecimal.valueOf(request.getCurrentPrice()))
        .rocket(request.isRocket())
        .build();

    Product savedProduct = productRepository.save(product);
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
}
