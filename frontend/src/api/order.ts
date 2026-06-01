import request from '../utils/request';
import type { JsonData, ConfirmOrderDto, ProductOrderDO, ProductOrderItemDO, PageResult } from '../types';

export const orderApi = {
  confirmOrder(data: ConfirmOrderDto): Promise<JsonData<string> | string> {
    return request.post('/order-service/api/productOrder/v1/confirm', data);
  },

  queryState(outTradeNo: string): Promise<JsonData<{ status: number }>> {
    return request.get('/order-service/api/productOrder/v1/query_state', { params: { out_trade_no: outTradeNo } });
  },

  repay(outTradeNo: string): Promise<JsonData<string>> {
    return request.get('/order-service/api/productOrder/v1/repay', { params: { out_trade_no: outTradeNo } });
  },

  getToken(): Promise<JsonData<string>> {
    return request.get('/order-service/api/productOrder/v1/get_token');
  },

  pageOrder(page: number, size: number): Promise<JsonData<PageResult<ProductOrderDO>>> {
    return request.get('/order-service/api/productOrder/v1/page', { params: { page, size } });
  },

  cancelOrder(outTradeNo: string): Promise<JsonData> {
    return request.get(`/order-service/api/productOrder/v1/cancel/${outTradeNo}`);
  },

  adminPageOrder(page: number, size: number): Promise<JsonData<PageResult<ProductOrderDO>>> {
    return request.get('/order-service/api/productOrder/v1/admin_page', { params: { page, size } });
  },

  detail(outTradeNo: string): Promise<JsonData<{ order: ProductOrderDO; items: ProductOrderItemDO[] }>> {
    return request.get(`/order-service/api/productOrder/v1/detail/${outTradeNo}`);
  },
};
