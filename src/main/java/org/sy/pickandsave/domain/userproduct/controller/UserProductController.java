package org.sy.pickandsave.domain.userproduct.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.sy.pickandsave.domain.userproduct.dto.UserProductResponse;
import org.sy.pickandsave.domain.userproduct.service.UserProductService;
import org.sy.pickandsave.global.common.ApiResponse;
import org.sy.pickandsave.global.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/user-products")
@RequiredArgsConstructor
public class UserProductController {

	private final UserProductService userProductService;


	/**
	 * 관심상품 목록 조회
	 */
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
	 * 관심상품 등록
	 */
	@PostMapping("/{productId}")
	public ResponseEntity<ApiResponse<Void>> addProduct(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long productId
	) {

		userProductService.addProduct(
				userDetails.getUserId(),
				productId
		);

		return ResponseEntity.ok(
				ApiResponse.success(null)
		);
	}


	/**
	 * 관심상품 삭제
	 */
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