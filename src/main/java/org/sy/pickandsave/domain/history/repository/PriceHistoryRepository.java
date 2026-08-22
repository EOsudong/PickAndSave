package org.sy.pickandsave.domain.history.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.history.entity.PriceHistory;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  List<PriceHistory> findByProductIdOrderByCreatedAtDesc(Long productId);
  List<PriceHistory> findByProductIdOrderByCreatedAtAsc(Long productId);

  // 평균가 통계 산출을 위한 누적 합계 및 건수 조회
  @Query("SELECT SUM(ph.price) FROM PriceHistory ph WHERE ph.product.id = :productId")
  Long sumPriceByProductId(@Param("productId") Long productId);

  Long countByProductId(Long productId);
}