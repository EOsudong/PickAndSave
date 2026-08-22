package org.sy.pickandsave.global.external.coupang;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.sy.pickandsave.global.external.coupang.dto.CoupangSearchResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
public class CoupangApiClient {

  private static final String SEARCH_PATH =
      "/v2/providers/affiliate_open_api/apis/openapi/products/search";

  private final RestClient restClient;
  private final CoupangHmacGenerator hmacGenerator;
  private final CoupangApiRateLimiter rateLimiter;

  @Value("${coupang.partners.domain}")
  private String domain;

  public CoupangApiClient(CoupangHmacGenerator hmacGenerator, CoupangApiRateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
    this.restClient = RestClient.create();
    this.hmacGenerator = hmacGenerator;
  }

  public CoupangSearchResponse searchProducts(String keyword, int limit) {
    //  1. 호출 전 메모리 기반 사전 통제 필터 구동 (네트워크 전송 차단)
    if (!rateLimiter.isAllowed()) {
      log.error("[API 호출 차단] 호출 가능 횟수를 초과했거나 Cooldown 상태입니다. 요청을 중단합니다.");
      throw new IllegalStateException("쿠팡 파트너스 API 호출 한도를 초과했습니다. 잠시 후 재시도해 주세요.");
    }

    String cleanDomain = domain.trim().replaceAll("/+$", "");
    if (!cleanDomain.startsWith("http://") && !cleanDomain.startsWith("https://")) {
      cleanDomain = "https://" + cleanDomain;
    }

    try {
      String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

      // path와 query를 분리 (서명용 / URI용 각각 다르게 사용)
      String query = String.format("keyword=%s&limit=%d", encodedKeyword, limit);

      // 서명 원문 생성 시에는 "?" 없이 path + query
      String authorizationHeader = hmacGenerator.generateAuthHeader("GET", SEARCH_PATH, query);

      // 실제 요청 URI에는 "?" 포함해서 조립
      URI requestUri = UriComponentsBuilder.fromUriString(cleanDomain + SEARCH_PATH + "?" + query)
          .build(true)
          .toUri();

      log.info("쿠팡 상품 검색 API 호출 시작 - Keyword: {}, Target URI: {}", keyword, requestUri);

      // 2. 호출 횟수 카운트 기록
      rateLimiter.recordCall();

      return restClient.method(HttpMethod.GET)
          .uri(requestUri)
          .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
          .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
          .retrieve()
          .onStatus(status -> status.value() >= 400, (req, res) -> {
            String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("쿠팡 API 호출 실패 [" + res.getStatusCode() + "]: " + body);
          })
          .body(CoupangSearchResponse.class);
    } catch (HttpClientErrorException.Forbidden e) {
      String responseBody = e.getResponseBodyAsString();
      log.error("[쿠팡 WAF 403 감지] - 응답 내용: {}", responseBody);

      // 3. WAF 403 Forbidden (Rate Limit 등) 감지 시 자동으로 Cooldown 모드 활성화
      rateLimiter.triggerCooldown();
      throw new IllegalStateException("쿠팡 보안 서버에 의해 차단되었습니다. 자동 Cooldown 시스템을 가동합니다.", e);

    } catch (Exception e) {
      log.error("쿠팡 API 통신 중 예외 발생: {}", e.getMessage(), e);
      throw new IllegalStateException("쿠팡 상품을 검색하는 과정에서 통신 장애가 발생했습니다.", e);
    }
  }

  /**
   * 쿠팡 파트너스 API를 통해 개별 상품의 최신 가격을 조회합니다.
   */
  public Long fetchCurrentPrice(Long coupangProductId, String productName) {
    log.info("쿠팡 실시간 최신 가격 조회 요청 - ProductID: {}, Name: {}", coupangProductId, productName);

    try {
      // [Step 1] 상품명 기반의 정밀 키워드로 1차 검색 실행
      if (productName != null && !productName.isBlank()) {
        String searchKeyword = cleanProductNameForSearch(productName);
        log.info("[Step 1] 상품명 노이즈 정제: '{}' ➔ '{}'", productName, searchKeyword);

        CoupangSearchResponse response = searchProducts(searchKeyword, 10); // 매칭 정합성을 위해 10개 수집

        if (response != null && response.getData() != null && response.getData().getProductData() != null) {
          Optional<Long> matchedPrice = response.getData().getProductData().stream()
              .filter(item -> item.getProductId().equals(coupangProductId))
              .map(CoupangSearchResponse.CoupangItem::getProductPrice)
              .findFirst();

          if (matchedPrice.isPresent()) {
            log.info("[성공] 상품명 검색으로 매칭 완료! ID: {}, 수집 가격: {}원", coupangProductId, matchedPrice.get());
            return matchedPrice.get();
          }
        }
        log.warn("[실패] 상품명 검색 결과에서 대상 ID({})를 찾을 수 없습니다. 2단계로 강제 백업 시도합니다.", coupangProductId);
      }

      // [Step 2] 차선책으로 상품 ID 자체를 키워드로 direct 검색 실행 (구버전 사양 백업)
      log.info("[Step 2] 상품 ID 직접 검색 가동 - Keyword: {}", coupangProductId);
      CoupangSearchResponse idResponse = searchProducts(String.valueOf(coupangProductId), 1);

      if (idResponse != null && idResponse.getData() != null && idResponse.getData().getProductData() != null) {
        Optional<Long> matchedPrice = idResponse.getData().getProductData().stream()
            .filter(item -> item.getProductId().equals(coupangProductId))
            .map(CoupangSearchResponse.CoupangItem::getProductPrice)
            .findFirst();

        if (matchedPrice.isPresent()) {
          log.info("[성공] 상품 ID 검색으로 매칭 성공! 수집 가격: {}원", matchedPrice.get());
          return matchedPrice.get();
        }
      }

      // [Step 3] 검색 결과가 완전히 비어있거나 매칭되지 않은 경우 (품절/비활성화 처리)
      throw new IllegalArgumentException("쿠팡에서 해당 상품 정보를 조회할 수 없습니다. (단종 또는 품절 상품 가능성)");

    } catch (Exception e) {
      log.error("쿠팡 단건 최신 가격 획득 완벽 실패 - ProductID: {}, Error: {}", coupangProductId, e.getMessage());
      throw new IllegalStateException("쿠팡 실시간 최신 가격을 가져오는 과정에서 통신 장애가 발생했습니다: " + e.getMessage(), e);
    }
  }

  /**
   * 검색엔진 노이즈 필터링 헬퍼 메서드
   * 대괄호, 소괄호, 특수옵션 등을 제거하고 검색엔진에 적합한 대표 상품 키워드로 변환합니다.
   */
  private String cleanProductNameForSearch(String productName) {
    if (productName == null) return "";
    // 1. 대괄호/소괄호 및 그 내부 설명 제거 (예: [쿠팡직수입] -> 빈값, (1개) -> 빈값)
    String cleaned = productName.replaceAll("\\[.*?\\]|\\(.*?\\)", "").trim();
    // 2. 검색에 역효과를 주는 유해 특수문자 제거
    cleaned = cleaned.replaceAll("[~!@#$%^&*()_+={}\\[\\]|\\\\:;\"'<>,.?/-]", " ");
    // 3. 공백 단위로 쪼개어 검색어 조율 (글자수가 너무 길면 검색이 매칭 안 되므로 대표 3개 단어 또는 최대 25자 제한)
    cleaned = cleaned.replaceAll("\\s+", " ").trim();
    if (cleaned.length() > 25) {
      cleaned = cleaned.substring(0, 25).trim();
    }
    return cleaned;
  }
}