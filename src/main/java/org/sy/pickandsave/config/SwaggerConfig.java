package org.sy.pickandsave.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sy.pickandsave.domain.products.dto.ProductCreateRequest;
import org.sy.pickandsave.domain.products.dto.ProductResponse;
import org.sy.pickandsave.domain.products.service.ProductService;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    String jwtSchemeName = "jwtAuth";

    // API 요청 헤더에 JWT 토큰 인증을 위한 SecurityRequirement 설정
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

    // SecuritySchemes 등록
    Components components = new Components()
        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
            .name(jwtSchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT"));

    return new OpenAPI()
        .info(apiInfo())
        .addSecurityItem(securityRequirement)
        .components(components);
  }

  private Info apiInfo() {
    return new Info()
        .title("PickAndSave API Specification")
        .description("쿠팡 파트너스 기반 가격 추적 및 알림 서비스 PickAndSave REST API 문서입니다.")
        .version("v1.0.0");
  }
}
