package org.sy.pickandsave.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CoupangClientConfig {

  @Bean
  public RestClient coupangRestClient(
      @Value("${coupang.partners.domain:https://api-gateway.coupang.com}") String domain
  ) {
    return RestClient.builder()
        .baseUrl(domain)
        .build();
  }
}