import request from '../utils/request';
import type { JsonData, PageResult, CouponDO, CouponRecordDO } from '../types';

export const couponApi = {
  pageCoupon(page: number = 1, size: number = 10): Promise<JsonData<PageResult<CouponDO>>> {
    return request.get('/coupon-service/api/coupon/v1/page_coupon', { params: { page, size } });
  },

  pageCouponRecord(page: number = 1, size: number = 10): Promise<JsonData<PageResult<CouponRecordDO>>> {
    return request.get('/coupon-service/api/couponRecord/v1/page_couponRecord', { params: { page, size } });
  },

  claimNewUserCoupon(couponId: number): Promise<JsonData> {
    return request.post('/coupon-service/api/couponRecord/v1/new_user_coupon', { couponId });
  },

  lockCouponRecords(couponRecordId: number, productIdList: number[]): Promise<JsonData> {
    return request.post('/coupon-service/api/couponRecord/v1/lock_records', {
      couponRecordId,
      productIdList,
    });
  },

  addPromotion(couponId: number): Promise<JsonData> {
    return request.put(`/coupon-service/api/coupon/v1/add/promotion/${couponId}`);
  },
};