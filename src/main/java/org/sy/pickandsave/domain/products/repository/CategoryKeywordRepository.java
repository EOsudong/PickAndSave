package org.sy.pickandsave.domain.products.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.products.entity.CategoryKeyword;

import java.util.List;

@Repository
public interface CategoryKeywordRepository extends JpaRepository<CategoryKeyword, Long> {
  @Override
  @Cacheable(value = "categoryKeywords") // 캐시 이름 지정
  List<CategoryKeyword> findAll();

}