import request from '../utils/request';
import type { JsonData, PageResult, ProductDO, ProductVO, BannerDO } from '../types';

export const productApi = {
  pageProduct(page: number = 1, size: number = 10): Promise<JsonData<PageResult<ProductVO>>> {
    return request.get('/product-service/api/product/v1/page_product', { params: { page, size } });
  },

  getDetail(productId: number): Promise<JsonData<ProductVO>> {
    return request.get(`/product-service/api/product/v1/detail/${productId}`);
  },

  search(keyword?: string, minPrice?: number, maxPrice?: number): Promise<JsonData<ProductVO[]>> {
    return request.get('/product-service/api/product/v1/search', { params: { keyword, minPrice, maxPrice } });
  },

  lockProduct(productId: number, count: number): Promise<JsonData> {
    return request.post('/product-service/api/product/v1/lock_product', { productId, count });
  },

  addProduct(data: ProductDO): Promise<JsonData> {
    return request.post('/product-service/api/product/v1/add_product', data);
  },

  updateProduct(data: ProductDO): Promise<JsonData> {
    return request.put('/product-service/api/product/v1/update_product', data);
  },

  deleteProduct(productId: number): Promise<JsonData> {
    return request.delete(`/product-service/api/product/v1/delete_product/${productId}`);
  },

  listBanners(): Promise<JsonData<BannerDO[]>> {
    return request.get('/product-service/api/banner/v1/list_banner');
  },

  addBanner(data: BannerDO): Promise<JsonData> {
    return request.post('/product-service/api/banner/v1/add_banner', data);
  },

  updateBanner(data: BannerDO): Promise<JsonData> {
    return request.put('/product-service/api/banner/v1/update_banner', data);
  },

  deleteBanner(id: number): Promise<JsonData> {
    return request.delete(`/product-service/api/banner/v1/delete_banner/${id}`);
  },
};