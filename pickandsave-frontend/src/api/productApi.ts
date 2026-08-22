import { apiClient } from './client';
import type { ApiResponse, ProductResponse } from '../types/api';

export async function getAllProducts(): Promise<ProductResponse[]> {
  const { data } = await apiClient.get<ApiResponse<ProductResponse[]>>('/api/products');
  return data.data;
}

export async function getProduct(id: number): Promise<ProductResponse> {
  const { data } = await apiClient.get<ApiResponse<ProductResponse>>(`/api/products/${id}`);
  return data.data;
}

export async function searchCoupangProducts(keyword: string, limit = 10): Promise<ProductResponse[]> {
  const { data } = await apiClient.post<ApiResponse<ProductResponse[]>>(
    '/api/products/search/coupang',
    null,
    { params: { keyword, limit } },
  );
  return data.data;
}
