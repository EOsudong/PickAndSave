package org.sy.pickandsave.domain.products.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.sy.pickandsave.domain.products.entity.CategoryKeyword;
import org.sy.pickandsave.domain.products.repository.CategoryKeywordRepository;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCategoryClassifier {

  private final CategoryKeywordRepository categoryKeywordRepository;

  /**
   * 상품명에 포함된 키워드를 찾아 카테고리 ID를 추론합니다.
   * 여러 키워드가 매칭되면 더 구체적인(긴) 키워드를 우선합니다. (예: "무선청소기" > "청소기")
   * 매칭되는 키워드가 없으면 Optional.empty() 반환 → categoryId는 null로 저장됨 (관리자가 나중에 수동 지정).
   */
  public Optional<Long> classify(String productName) {
    if (productName == null || productName.isBlank()) {
      return Optional.empty();
    }

    String normalized = productName.replaceAll("\\s+", "").toLowerCase();

    return categoryKeywordRepository.findAll().stream()
        .sorted(Comparator.comparingInt((CategoryKeyword k) -> k.getKeyword().length()).reversed())
        .filter(k -> normalized.contains(k.getKeyword().toLowerCase()))
        .findFirst()
        .map(k -> k.getCategory().getId());
  }
}