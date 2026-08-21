package org.sy.pickandsave.global.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.sy.pickandsave.global.common.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 1. DTO @Valid 유효성 검증 실패 예외
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
    String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    log.warn("Validation error: {}", errorMessage);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(errorMessage));
  }

  // 2. 잘못된 JSON 요청 처리 (ture 오타와 같은 문법/파싱 에러)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    log.warn("JSON Parse Error: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("요청한 JSON 형식이 올바르지 않습니다. (필드명, 데이터 타입, true/false 오타 확인)"));
  }

  // 3. 서비스 비즈니스 로직 상 예외 (중복 상품, 존재하지 않는 데이터 등)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("Business Logic Error: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getMessage()));
  }

  // 4. 기타 예기치 못한 시스템 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
    log.error("Unhandled Internal Server Error", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."));
  }
}
