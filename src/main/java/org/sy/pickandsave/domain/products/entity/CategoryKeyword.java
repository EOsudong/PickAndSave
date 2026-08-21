package org.sy.pickandsave.domain.products.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CATEGORY_KEYWORDS", indexes = {
    @Index(name = "idx_category_keywords_keyword", columnList = "keyword")
})
@Getter
@NoArgsConstructor
public class CategoryKeyword {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_keywords_seq_gen")
  @SequenceGenerator(name = "category_keywords_seq_gen", sequenceName = "CATEGORY_KEYWORDS_SEQ", allocationSize = 1)
  private Long id;

  /**
   * 상품명에 포함되면 매칭되는 키워드 (예: "청소기")
   */
  @Column(name = "keyword", length = 100, nullable = false, unique = true)
  private String keyword;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private ProductCategory category;

  @Builder
  public CategoryKeyword(String keyword, ProductCategory category) {
    this.keyword = keyword;
    this.category = category;
  }
}