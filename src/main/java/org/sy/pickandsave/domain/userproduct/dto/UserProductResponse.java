package org.sy.pickandsave.domain.userproduct.dto;

import lombok.Builder;
import lombok.Getter;
import org.sy.pickandsave.domain.userproduct.entity.UserProduct;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProductResponse {

	private Long id;

	private Long productId;

	private String productName;

	private String imageUrl;

	private Long currentPrice;

	private Long lowestPrice;

	private Long highestPrice;

	private String partnersAffiliateUrl;

	private boolean rocket;

	private LocalDateTime createdAt;


	public static UserProductResponse from(
			UserProduct userProduct
	) {

		var product = userProduct.getProduct();

		return UserProductResponse.builder()
				.id(userProduct.getId())
				.productId(product.getId())
				.productName(product.getProductName())
				.imageUrl(product.getImageUrl())
				.currentPrice(product.getCurrentPrice())
				.lowestPrice(product.getLowestPrice())
				.highestPrice(product.getHighestPrice())
				.partnersAffiliateUrl(
						product.getPartnersAffiliateUrl()
				)
				.rocket(product.isRocket())
				.createdAt(userProduct.getCreatedAt())
				.build();
	}
	
}