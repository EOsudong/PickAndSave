import { ProductResponse, derivePriceStatus } from '../types/api';
import SignalBar from './SignalBar';

function formatPrice(n: number): string {
  return n.toLocaleString('ko-KR');
}

export default function ProductCard({ product }: { product: ProductResponse }) {
  const status = derivePriceStatus(product);
  const discount =
    product.highestPrice > 0
      ? Math.round((1 - product.currentPrice / product.highestPrice) * 100)
      : 0;

  return (
    <a
      href={product.partnersAffiliateUrl || product.coupangProductUrl}
      target="_blank"
      rel="noopener noreferrer"
      className="block rounded-card border border-line bg-white transition-transform hover:-translate-y-0.5"
    >
      <div className="relative flex aspect-square items-center justify-center rounded-t-card bg-[#F0EFEA]">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.productName} className="h-full w-full rounded-t-card object-cover" />
        ) : (
          <span className="text-sm text-ink-soft">상품 이미지</span>
        )}

        {product.rocket && (
          <span className="absolute left-2.5 top-2.5 rounded-full bg-trust px-2 py-1 text-[11px] font-semibold text-white">
            로켓배송
          </span>
        )}
        {status === 'best' && (
          <span className="absolute right-2.5 top-2.5 rounded-full bg-drop-bg px-2 py-1 text-[11px] font-bold text-drop">
            역대 최저가
          </span>
        )}
      </div>

      <div className="p-4">
        <p className="mb-2.5 line-clamp-2 h-[38px] text-sm leading-snug text-ink">{product.productName}</p>

        <div className="mb-1 flex items-baseline gap-1.5">
          {discount > 0 && <span className="text-[15px] font-bold text-rise">{discount}%</span>}
          <span className="text-xl font-bold text-ink">{formatPrice(product.currentPrice)}원</span>
        </div>

        <p className="mb-2.5 text-xs text-ink-soft">역대 최저 {formatPrice(product.lowestPrice)}원</p>

        <SignalBar status={status} size="sm" />
      </div>
    </a>
  );
}
