package org.sy.pickandsave.domain.userproduct.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sy.pickandsave.domain.userproduct.dto.UserProductResponse;
import org.sy.pickandsave.domain.userproduct.service.UserProductService;
import org.sy.pickandsave.global.common.ApiResponse;
import org.sy.pickandsave.global.security.CustomUserDetails;

import java.util.List;

@Tag(
    name = "UserProduct",
    description = "사용자 관심상품 관리 API"
)
@RestController
@RequestMapping("/api/user-products")
@RequiredArgsConstructor
public class UserProductController {

  private final UserProductService userProductService;

  /**
   * 관심상품 등록
   */
  @Operation(
      summary = "관심상품 등록",
      description = "사용자가 상품을 관심상품으로 등록합니다."
  )
  @PostMapping("/{productId}")
  public ResponseEntity<ApiResponse<Void>> addProduct(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long productId
  ) {

    userProductService.addProduct(
        userDetails.getUserId(),
        productId
    );

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(null));
  }

  /**
   * 관심상품 목록 조회
   */
  @Operation(
      summary = "관심상품 목록 조회",
      description = "사용자가 등록한 관심상품 목록을 조회합니다."
  )
  @GetMapping
  public ResponseEntity<ApiResponse<List<UserProductResponse>>> getUserProducts(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {

    List<UserProductResponse> response =
        userProductService.getUserProducts(
            userDetails.getUserId()
        );

    return ResponseEntity.ok(
        ApiResponse.success(response)
    );
  }

  /**
   * 관심상품 개수 조회
   * */
  @Operation(
      summary = "관심상품 개수 조회",
      description = "사용자가 등록한 관심상품 개수를 조회합니다."
  )
  @GetMapping("/count")
  public ResponseEntity<ApiResponse<Long>> getUserProductCount(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {

    long count =
        userProductService.getUserProductCount(userDetails.getUserId());

    return ResponseEntity.ok(
        ApiResponse.success(count)
    );
  }


  /**
   * 관심상품 삭제
   */
  @Operation(
      summary = "관심상품 삭제",
      description = "사용자의 관심상품을 삭제합니다."
  )
  @DeleteMapping("/{productId}")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long productId
  ) {

    userProductService.deleteProduct(
        userDetails.getUserId(),
        productId
    );

    return ResponseEntity.ok(
        ApiResponse.success(null)
    );
  }
}