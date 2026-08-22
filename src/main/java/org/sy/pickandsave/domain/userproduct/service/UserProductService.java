package org.sy.pickandsave.domain.userproduct.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sy.pickandsave.domain.products.entity.Product;
import org.sy.pickandsave.domain.products.repository.ProductRepository;
import org.sy.pickandsave.domain.userproduct.dto.UserProductResponse;
import org.sy.pickandsave.domain.userproduct.entity.UserProduct;
import org.sy.pickandsave.domain.userproduct.repository.UserProductRepository;
import org.sy.pickandsave.domain.users.entity.User;
import org.sy.pickandsave.domain.users.entity.UserPlan;
import org.sy.pickandsave.domain.users.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProductService {

	private final UserProductRepository userProductRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	/**
	 * 관심상품 등록
	 */
	@Transactional
	public void addProduct(Long userId, Long productId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

		// 이미 등록된 상품인지 확인
		if (userProductRepository.existsByUserIdAndProductId(userId, productId)) {
			throw new RuntimeException("이미 관심상품으로 등록된 상품입니다.");
		}

		// FREE 회원은 최대 10개
		if (user.getPlan() == UserPlan.FREE) {

			long count = userProductRepository.countByUserId(userId);

			if (count >= 10) {
				throw new RuntimeException(
						"무료 회원은 관심상품을 최대 10개까지 등록할 수 있습니다."
				);
			}
		}


		UserProduct userProduct =
				new UserProduct(user, product);

		userProductRepository.save(userProduct);
	}

	/**
	 * 관심상품 목록 조회
	 */
	public List<UserProductResponse> getUserProducts(Long userId) {
		return userProductRepository.findByUserId(userId)
				.stream()
				.map(UserProductResponse::from)
				.toList();
	}

	/**
	 * 관심상품 개수 조회
	 */
	public long getUserProductCount(Long userId) {

		return userProductRepository.countByUserId(userId);
	}

	/**
	 * 관심상품 삭제
	 */
	@Transactional
	public void deleteProduct(
			Long userId,
			Long productId
	) {

		if (!userProductRepository
				.existsByUserIdAndProductId(userId, productId)) {

			throw new IllegalArgumentException(
					"관심상품으로 등록되지 않은 상품입니다."
			);
		}

		userProductRepository
				.deleteByUserIdAndProductId(
						userId,
						productId
				);
	}

}
