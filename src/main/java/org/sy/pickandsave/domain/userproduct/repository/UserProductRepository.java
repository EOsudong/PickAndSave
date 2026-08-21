package org.sy.pickandsave.domain.userproduct.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sy.pickandsave.domain.userproduct.entity.UserProduct;

import java.util.List;

@Repository
public interface UserProductRepository
		extends JpaRepository<UserProduct, Long> {

	List<UserProduct> findByUserId(Long userId);

	long countByUserId(Long userId);

	boolean existsByUserIdAndProductId(Long userId, Long productId);

	void deleteByUserIdAndProductId(Long userId, Long productId);
}