package org.sy.pickandsave.global.external.coupang;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.sy.pickandsave.global.external.coupang.dto.CoupangSearchResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CoupangApiClient {

  private static final String SEARCH_PATH =
      "/v2/providers/affiliate_open_api/apis/openapi/products/search";

  private final RestClient restClient;
  private final CoupangHmacGenerator hmacGenerator;

  @Value("${coupang.partners.domain}")
  private String domain;

  public CoupangApiClient(CoupangHmacGenerator hmacGenerator) {
    this.restClient = RestClient.create();
    this.hmacGenerator = hmacGenerator;
  }

  public CoupangSearchResponse searchProducts(String keyword, int limit) {
    String cleanDomain = domain.trim().replaceAll("/+$", "");
    if (!cleanDomain.startsWith("http://") && !cleanDomain.startsWith("https://")) {
      cleanDomain = "https://" + cleanDomain;
    }

    String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

    // path와 query를 분리 (서명용 / URI용 각각 다르게 사용)
    String query = String.format("keyword=%s&limit=%d", encodedKeyword, limit);

    // 서명 원문 생성 시에는 "?" 없이 path + query
    String authorizationHeader = hmacGenerator.generateAuthHeader("GET", SEARCH_PATH, query);

    // 실제 요청 URI에는 "?" 포함해서 조립
    URI requestUri = UriComponentsBuilder.fromUriString(cleanDomain + SEARCH_PATH + "?" + query)
        .build(true)
        .toUri();

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
  }
}