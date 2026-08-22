export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}

export type PriceStatus = 'best' | 'normal' | 'high';

export interface ProductResponse {
  id: number;
  coupangProductId: number;
  productName: string;
  coupangProductUrl: string;
  partnersAffiliateUrl: string;
  imageUrl: string | null;
  categoryId: number | null;
  currentPrice: number;
  lowestPrice: number;
  highestPrice: number;
  averagePrice: number;
  rocket: boolean;
  lastCheckedAt: string | null;
  createdAt: string;
}

/**
 * 백엔드는 아직 "구매 적기" 상태를 직접 내려주지 않아요.
 * currentPrice와 lowestPrice/highestPrice를 비교해 프론트에서 임시로 계산합니다.
 * (추후 백엔드에서 계산된 status 필드를 내려주면 이 로직은 제거하면 됩니다.)
 */
export function derivePriceStatus(p: Pick<ProductResponse, 'currentPrice' | 'lowestPrice' | 'highestPrice'>): PriceStatus {
  const range = p.highestPrice - p.lowestPrice;
  if (range <= 0) return 'normal';

  const position = (p.currentPrice - p.lowestPrice) / range;

  if (position <= 0.15) return 'best';
  if (position >= 0.6) return 'high';
  return 'normal';
}
