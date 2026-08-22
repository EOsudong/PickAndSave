import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Search, ChevronRight, Zap } from 'lucide-react';
import Header from '../components/Header';
import ProductCard from '../components/ProductCard';
import SignalBar from '../components/SignalBar';
import { getAllProducts, searchCoupangProducts } from '../api/productApi';

const CATEGORIES = ['가전/디지털', '생활용품', '패션/뷰티', '식품', '유아/키즈', '스포츠/레저', '가구/인테리어'];

export default function HomePage() {
  const [keyword, setKeyword] = useState('');
  const queryClient = useQueryClient();

  const productsQuery = useQuery({
    queryKey: ['products'],
    queryFn: getAllProducts,
  });

  const searchMutation = useMutation({
    mutationFn: (kw: string) => searchCoupangProducts(kw, 10),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = keyword.trim();
    if (!trimmed) return;
    searchMutation.mutate(trimmed);
  }

  const products = productsQuery.data ?? [];

  return (
    <div className="min-h-screen bg-paper">
      <Header />

      <section className="mx-auto max-w-[1040px] px-5 pb-10 pt-14 text-center">
        <h1 className="mb-3 text-[32px] font-extrabold leading-snug text-ink">
          항상 최적의 가격으로 구매하세요!
        </h1>
        <p className="mb-8 text-base text-ink-soft">
          관심 상품을 등록하면 가격이 떨어질 때 알려드려요
        </p>

        <form onSubmit={handleSearch} className="relative mx-auto mb-7 max-w-[600px]">
          <Search
            size={20}
            aria-hidden="true"
            className="pointer-events-none absolute left-[18px] top-1/2 -translate-y-1/2 text-ink-soft"
          />
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="상품명을 검색해보세요"
            className="h-14 w-full rounded-2xl border-[1.5px] border-line bg-white pl-[50px] pr-5 text-base outline-none focus:border-trust"
          />
        </form>

        {searchMutation.isPending && (
          <p className="text-sm text-ink-soft">쿠팡에서 상품을 찾고 있어요...</p>
        )}
        {searchMutation.isError && (
          <p className="text-sm text-rise">검색에 실패했어요. 잠시 후 다시 시도해주세요.</p>
        )}
        {searchMutation.isSuccess && (
          <p className="text-sm text-drop">
            {searchMutation.data.length}개의 신규 상품을 등록했어요.
          </p>
        )}

        {!searchMutation.isPending && !searchMutation.isSuccess && (
          <div className="inline-flex items-center gap-3.5 rounded-xl border border-line bg-white px-4 py-2.5">
            <Zap size={16} aria-hidden="true" className="text-trust" />
            <span className="text-[13px] text-ink-soft">무선 멀티 핸디 청소기</span>
            <SignalBar status="best" size="sm" />
          </div>
        )}
      </section>

      <section className="mx-auto max-w-[1040px] px-5 pb-2 pt-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-ink">등록된 상품</h2>
          <a href="#" className="flex items-center gap-0.5 text-[13px] text-ink-soft">
            더보기 <ChevronRight size={14} aria-hidden="true" />
          </a>
        </div>

        {productsQuery.isLoading && <p className="py-10 text-center text-sm text-ink-soft">불러오는 중이에요...</p>}
        {productsQuery.isError && (
          <p className="py-10 text-center text-sm text-rise">상품 목록을 불러오지 못했어요.</p>
        )}
        {productsQuery.isSuccess && products.length === 0 && (
          <p className="py-10 text-center text-sm text-ink-soft">
            아직 등록된 상품이 없어요. 위 검색창에서 상품을 찾아보세요.
          </p>
        )}

        <div className="grid grid-cols-[repeat(auto-fit,minmax(220px,1fr))] gap-4">
          {products.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-[1040px] px-5 pb-16 pt-9">
        <h2 className="mb-4 text-xl font-bold text-ink">카테고리 둘러보기</h2>
        <div className="flex flex-wrap gap-2.5">
          {CATEGORIES.map((c) => (
            <button
              key={c}
              className="rounded-full border border-line bg-white px-4 py-2.5 text-sm text-ink"
            >
              {c}
            </button>
          ))}
        </div>
      </section>

      <footer className="border-t border-line px-5 py-6 text-center">
        <p className="text-xs text-ink-soft">
          PickAndSave · 쿠팡 파트너스 활동을 통해 일정액의 수수료를 제공받을 수 있습니다
        </p>
      </footer>
    </div>
  );
}
