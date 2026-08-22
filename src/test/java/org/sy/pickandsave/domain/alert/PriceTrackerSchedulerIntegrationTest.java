package org.sy.pickandsave.domain.alert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.alert.entity.Notification;
import org.sy.pickandsave.domain.alert.entity.PriceAlert;
import org.sy.pickandsave.domain.alert.repository.NotificationRepository;
import org.sy.pickandsave.domain.alert.repository.PriceAlertRepository;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.products.repository.ProductRepository;
import org.sy.pickandsave.domain.products.service.ProductPriceUpdateService;
import org.sy.pickandsave.domain.users.entity.AuthProvider;
import org.sy.pickandsave.domain.users.entity.User;
import org.sy.pickandsave.domain.users.entity.UserPlan;
import org.sy.pickandsave.domain.users.repository.UserRepository;
import org.sy.pickandsave.global.external.coupang.CoupangApiClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PriceTrackerSchedulerIntegrationTest {

  @Autowired
  private ProductPriceUpdateService priceUpdateService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private PriceAlertRepository priceAlertRepository;

  @Autowired
  private NotificationRepository notificationRepository;

  @MockitoBean
  private CoupangApiClient coupangApiClient; // 외부 API 호출 통제용 모킹

  @Test
  @DisplayName("정기 가격 갱신 결과가 사용자가 등록한 알림 목표가 이하로 떨어지면, 알림 상태가 비활성화되고 알림 이력이 성공적으로 생성된다")
  void priceAlertTriggerAndNotificationSuccess() {
    // [1] Given - 테스트 기초 데이터 설계
    // 1. 임시 테스트 유저 생성 및 저장
    User testUser = User.builder()
        .email("tester@example.com")
        .nickname("가격추적왕")
        .provider(AuthProvider.LOCAL)
        .plan(UserPlan.FREE)
        .build();
    userRepository.save(testUser);

    // 2. 임시 추적 대상 상품 생성 및 저장 (기존가: 1,000,000원)
    Product testProduct = Product.builder()
        .coupangProductId(999888777L)
        .productName("최고급 태블릿 PC")
        .coupangProductUrl("https://coupang.com/products/999888777")
        .currentPrice(1000000L)
        .lowestPrice(1000000L)
        .highestPrice(1000000L)
        .averagePrice(BigDecimal.valueOf(1000000L))
        .rocket(true)
        .build();
    productRepository.save(testProduct);

    // 3. 유저의 목표 가격 알림 설정 생성 및 저장 (목표가: 900,000원)
    PriceAlert alert = PriceAlert.builder()
        .user(testUser)
        .product(testProduct)
        .targetPrice(900000L) // 90만원 이하로 떨어질 시 알림 받기 원함
        .alertType("EMAIL")
        .active(true)
        .build();
    priceAlertRepository.save(alert);

    // 4. 모킹 설정: 쿠팡 API 최신 조회 결과가 850,000원으로 대폭 하락했다고 가정
    Mockito.when(coupangApiClient.fetchCurrentPrice(testProduct.getCoupangProductId(), testProduct.getProductName()))
        .thenReturn(850000L);

    // [2] When - 가격 자동 갱신 프로세스 구동
    priceUpdateService.updateProductPrice(testProduct);

    // [3] Then - DB 정합성 검증
    // 1. 상품의 최신 가격 정보가 850,000원으로 갱신되었는지 검증
    Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
    assertThat(updatedProduct.getCurrentPrice()).isEqualTo(850000L);
    assertThat(updatedProduct.getLowestPrice()).isEqualTo(850000L); // 신기록 수립 검증

    // 2. 알림 설정 상태가 발동 완료되어 active = false 상태로 변경되었는지 검증
    PriceAlert triggeredAlert = priceAlertRepository.findById(alert.getId()).orElseThrow();
    assertThat(triggeredAlert.isActive()).isFalse();
    assertThat(triggeredAlert.getTriggeredAt()).isNotNull();

    // 3. NOTIFICATIONS 테이블에 발송 준비 및 이력 기록이 정상 적재되었는지 검증
    List<Notification> notifications = notificationRepository.findAll();
    assertThat(notifications).isNotEmpty();

    Notification targetNotification = notifications.stream()
        .filter(n -> n.getUser().getId().equals(testUser.getId()))
        .findFirst()
        .orElseThrow();

    assertThat(targetNotification.getTitle()).contains("가격 인하");
    assertThat(targetNotification.getMessage()).contains("850000"); // 갱신된 현재가 포함 여부
    assertThat(targetNotification.getStatus()).isEqualTo("SENT"); // 전송 성공 마크 검증
  }
}