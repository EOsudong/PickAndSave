package org.sy.pickandsave.domain.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.alert.entity.Notification;
import org.sy.pickandsave.domain.alert.entity.PriceAlert;
import org.sy.pickandsave.domain.alert.repository.NotificationRepository;
import org.sy.pickandsave.domain.alert.repository.PriceAlertRepository;
import org.sy.pickandsave.domain.products.entity.Product;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PriceAlertService {

  private final PriceAlertRepository priceAlertRepository;
  private final NotificationRepository notificationRepository;

  /**
   * 가격 변동이 감지된 상품의 활성 목표가 알림 설정을 검사하여 발송 처리를 수행합니다.
   */
  public void checkAndTriggerAlerts(Product product, Long currentPrice) {
    // 1. 해당 상품에 걸려 있는 활성화(active = true)된 모든 유저 알림 설정을 가져옴 (Join Fetch 유저 정보 포함)
    List<PriceAlert> activeAlerts = priceAlertRepository.findActiveAlertsByProductId(product.getId());

    if (activeAlerts.isEmpty()) {
      return;
    }

    log.info("목표가 도달 알림 검사 시작 - 상품: {}, 활성 알림 수: {}개", product.getProductName(), activeAlerts.size());

    for (PriceAlert alert : activeAlerts) {
      // 2. 유저가 설정한 목표가 도달 여부 확인 (현재가 <= 목표가)
      if (currentPrice <= alert.getTargetPrice()) {
        log.info("[목표가 도달!] 유저 ID: {} | 설정 목표가: {}원 >= 현재가: {}원",
            alert.getUser().getId(), alert.getTargetPrice(), currentPrice);

        // 3. 알림 설정 상태 비활성화 (1회성 알림 중복 발송 방지 기법)
        alert.trigger();

        // 4. 알림 전송 이력(NOTIFICATIONS) 데이터 적재
        String title = String.format("🎉 찜하신 상품 가격 인하! [%s]", product.getProductName());
        String message = String.format("설정하신 목표가 %s원보다 저렴한 %s원에 지금 구매하실 수 있습니다! 놓치지 마세요!",
            alert.getTargetPrice(), currentPrice);

        Notification notification = Notification.builder()
            .user(alert.getUser())
            .product(product)
            .priceAlert(alert)
            .notificationType(alert.getAlertType())
            .title(title)
            .message(message)
            .status("PENDING") // 대기 상태로 등록 후 외부 발송 시 발송 성공 여부 수정
            .build();

        notificationRepository.save(notification);

        // 5. [비동기 발송 확장부] 외부 전송 게이트웨이 연동 유도
        sendExternalNotification(notification);
      }
    }
  }

  /**
   * 알림 유형별 외부 발송 시스템(이메일 전송 컴포넌트, 카카오 알림톡 API 등)을 연동하는 통합 추상 통로입니다.
   */
  private void sendExternalNotification(Notification notification) {
    String type = notification.getNotificationType();
    log.info("비동기 외부 알림 발송 요청 시작 - 타입: {}, 수신 유저: {}", type, notification.getUser().getEmail());

    try {
      switch (type.toUpperCase()) {
        case "EMAIL":
          // JavaMailSender 연동 및 메일 발송 로직 위치
          log.info("[이메일 전송 시뮬레이션] TO: {} | TITLE: {}", notification.getUser().getEmail(), notification.getTitle());
          break;
        case "KAKAO":
          // 카카오 비즈니스 알림톡 연동 모듈 호출 위치
          log.info("[카카오 알림톡 전송 시뮬레이션] TO: {}", notification.getUser().getNickname());
          break;
        case "WEB_PUSH":
          // WebPushService(FCM/VAPID) 모듈 호출 위치
          log.info("[웹 푸시 전송 시뮬레이션] TITLE: {}", notification.getTitle());
          break;
        default:
          log.warn("지원하지 않는 알림 유형입니다: {}", type);
      }
      // 발송 성공 마크
      notification.markAsSent();

    } catch (Exception e) {
      log.error("외부 알림 발송 중 통신 실패 - 알림 ID: {}, 오류: {}", notification.getId(), e.getMessage());
      notification.markAsFailed();
    }
  }
}