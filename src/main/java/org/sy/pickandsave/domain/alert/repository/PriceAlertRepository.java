package org.sy.pickandsave.domain.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.alert.entity.PriceAlert;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

  /**
   * 특정 상품에 걸려 있는 활성화 상태의 모든 목표가 알림 설정을 조회합니다.
   * N+1 문제를 원천 차단하기 위해 유저 정보(User)를 한 번에 Join Fetch 합니다.
   */
  @Query("SELECT pa FROM PriceAlert pa JOIN FETCH pa.user WHERE pa.product.id = :productId AND pa.active = true")
  List<PriceAlert> findActiveAlertsByProductId(@Param("productId") Long productId);
}