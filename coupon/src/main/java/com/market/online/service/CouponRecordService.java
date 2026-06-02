package com.market.online.service;

import com.market.online.model.CouponRecordDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.market.online.model.CouponRecordMessage;
import com.market.online.request.LockCouponRecordRequest;
import com.market.online.request.NewUserCouponRequest;
import com.market.online.util.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LZX
 * @since 2025-02-14
 */
public interface CouponRecordService extends IService<CouponRecordDO> {

    JsonData selectCouponRecord(Long page, Long size);

    JsonData detailCouponRecord(Long recordId);

    JsonData newUserCoupon(NewUserCouponRequest newUserCouponRequest);

    JsonData lockRecords(LockCouponRecordRequest lockRecords);

    boolean releaseCouponRecord(CouponRecordMessage recordMessage);
}
