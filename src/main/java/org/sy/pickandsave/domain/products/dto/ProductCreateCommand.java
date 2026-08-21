package org.sy.pickandsave.domain.products.dto;

import lombok.Builder;

@Builder
public record ProductCreateCommand(
    Long coupangProductId,
    String productName,
    String coupangProductUrl,
    String partnersAffiliateUrl,
    String imageUrl,
    Long categoryId,
    Long currentPrice,
    boolean rocket
) {
}