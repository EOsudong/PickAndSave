package org.sy.pickandsave.global.external.coupang;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class CoupangApiRateLimiter {

  // 시간당 최대 허용 호출 수 (기본값 50회로 안전 마진 확보)
  @Value("${coupang.partners.max-calls-per-hour:50}")
  private int maxCallsPerHour;

  // 403 Rate Limit 감지 시 잠금 시간 (기본값 10분)
  @Value("${coupang.partners.cooldown-minutes:10}")
  private int cooldownMinutes;

  // 스레드 안전한 실시간 타임스탬프 슬라이딩 윈도우 큐
  private final Queue<Long> callTimestamps = new ConcurrentLinkedQueue<>();

  // 강제 임시 잠금 시간 타임스탬프
  private volatile Instant blockedUntil = null;

  /**
   * API 요청 전, 현재 전송이 안전한지 사전 검증합니다.
   */
  public synchronized boolean isAllowed() {
    Instant now = Instant.now();

    // 1. 임시 Cooldown 잠금 상태 체크
    if (blockedUntil != null) {
      if (now.isBefore(blockedUntil)) {
        log.warn("[차단 우회 방지] 현재 쿠팡 API 호출 제한에 도달해 임시 잠금 상태입니다. 차단 해제 예정 시간: {}", blockedUntil);
        return false;
      } else {
        log.info("[차단 자동 해제] Cooldown 격리 시간이 경과하여 API 호출을 다시 허용합니다.");
        blockedUntil = null;
      }
    }

    // 2. 1시간(3600초) 슬라이딩 윈도우 갱신
    long oneHourAgo = now.toEpochMilli() - (3600 * 1000);
    while (!callTimestamps.isEmpty() && callTimestamps.peek() < oneHourAgo) {
      callTimestamps.poll();
    }

    // 3. 시간당 최대 호출 한도 도달 여부 판별
    if (callTimestamps.size() >= maxCallsPerHour) {
      log.warn("[호출 사전 차단] 시간당 최대 API 호출 한도에 도달했습니다! (설정한 한도: {}회/시간, 현재 누적: {}회)",
          maxCallsPerHour, callTimestamps.size());
      return false;
    }

    return true;
  }

  /**
   * 호출 카운트를 안전하게 1 누적합니다.
   */
  public void recordCall() {
    callTimestamps.add(Instant.now().toEpochMilli());
    log.info("[API 호출 기록] 최근 1시간 내 누적 호출 횟수: {} / {}", callTimestamps.size(), maxCallsPerHour);
  }

  /**
   * 403 Unauthorized (Rate-Limit) 발생 시 즉각 안전 냉각기를 가동합니다.
   */
  public void triggerCooldown() {
    this.blockedUntil = Instant.now().plusSeconds(cooldownMinutes * 10L);
    log.error("[RATE LIMIT 경고 가동] 쿠팡 파트너스 API 403 Rate-Limit이 외부 응답으로 확인되어 향후 {}분간 모든 요청을 강제 기각합니다. (해제 예정: {})",
        cooldownMinutes, blockedUntil);
  }

  /**
   * 수동 리셋 유틸리티 (테스트 및 관리자 복구용)
   */
  public void reset() {
    callTimestamps.clear();
    blockedUntil = null;
    log.info("[초기화 완료] API 호출 한도 기록 및 강제 잠금이 성공적으로 초기화되었습니다.");
  }

  public int getCurrentWindowCalls() {
    return callTimestamps.size();
  }
}