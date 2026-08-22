package org.sy.pickandsave.domain.history.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.history.entity.PriceHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  // 상품의 가장 최근 가격 이력을 한 건 조회 (중복 기록 검증용)
  Optional<PriceHistory> findFirstByProductIdOrderByRecordedAtDesc(Long productId);

  // 시간순(오름차순) 차트 표시용 이력 조회
  List<PriceHistory> findByProductIdOrderByRecordedAtAsc(Long productId);

  // 평균가 통계 산출을 위한 누적 합계 조회
  @Query("SELECT SUM(ph.price) FROM PriceHistory ph WHERE ph.product.id = :productId")
  Long sumPriceByProductId(@Param("productId") Long productId);

  // 이력 총 건수 조회
  Long countByProductId(Long productId);
}