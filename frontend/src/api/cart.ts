import request from '../utils/request';
import type { JsonData, CartItemVO, CartItemDto, CartDO } from '../types';

export const cartApi = {
  addCart(data: CartItemDto): Promise<JsonData> {
    return request.post('/product-service/api/cart/v1/add', data);
  },

  getCart(): Promise<JsonData<CartDO>> {
    return request.get('/product-service/api/cart/v1/myCart');
  },

  clearCart(): Promise<JsonData> {
    return request.delete('/product-service/api/cart/v1/clear');
  },

  updateCart(data: CartItemDto): Promise<JsonData> {
    return request.put('/product-service/api/cart/v1/change', data);
  },

  deleteCartItem(productId: number): Promise<JsonData> {
    return request.delete(`/product-service/api/cart/v1/del/${productId}`);
  },

  confirmOrderCartItems(productIdList: number[]): Promise<JsonData<CartItemVO[]>> {
    return request.post('/product-service/api/cart/v1/confirm_order_cart_items', productIdList);
  },
};