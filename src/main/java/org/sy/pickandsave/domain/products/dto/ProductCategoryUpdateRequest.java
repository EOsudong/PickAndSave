package org.sy.pickandsave.domain.products.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductCategoryUpdateRequest {

  @NotNull(message = "카테고리 ID는 필수입니다.")
  private Long categoryId;
}