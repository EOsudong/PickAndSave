package org.sy.pickandsave.domain.products.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.products.entity.CategoryKeyword;
import org.sy.pickandsave.domain.products.repository.CategoryKeywordRepository;

@Service
@RequiredArgsConstructor
public class CategoryKeywordAdminService {

  private final CategoryKeywordRepository categoryKeywordRepository;

  @Transactional
  @CacheEvict(value = "categoryKeywords", allEntries = true) // CUD 발생 시 categoryKeywords 캐시 전체 삭제
  public void saveKeyword(CategoryKeyword keyword) {
    categoryKeywordRepository.save(keyword);
  }
}