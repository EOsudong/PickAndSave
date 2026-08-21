package org.sy.pickandsave.global.common;

import lombok.Getter;

import java.time.LocalDateTime;

//공통 응답 DTO
@Getter
public class ApiResponse<T> {
  private final boolean success;
  private final T data;
  private final String message;
  private final LocalDateTime timestamp;

  private ApiResponse(boolean success, T data, String message) {
    this.success = success;
    this.data = data;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, "요청이 성공적으로 처리되었습니다.");
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, null, message);
  }
}
