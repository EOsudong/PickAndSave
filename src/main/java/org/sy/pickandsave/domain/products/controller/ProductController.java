package org.sy.pickandsave.domain.products.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sy.pickandsave.domain.products.dto.ProductCreateRequest;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.service.ProductService;

import java.util.List;

@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "상품 등록", description = "신규 쿠팡 상품 정보를 DB에 등록합니다.")
  @PostMapping
  public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
    ProductResponse response = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "상품 단건 조회", description = "상품 ID(PK)를 이용해 해당 상품의 상세 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long id) {
    ProductResponse response = productService.getProductById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "상품 전체 목록 조회", description = "등록된 전체 상품 리스트를 조회합니다.")
  @GetMapping
  public ResponseEntity<List<ProductResponse>> getAllProducts() {
    List<ProductResponse> response = productService.getAllProducts();
    return ResponseEntity.ok(response);
  }
}
