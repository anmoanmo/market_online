package com.buka.service;

import com.buka.model.CouponRecordDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.buka.model.CouponRecordMessage;
import com.buka.request.LockCouponRecordRequest;
import com.buka.request.NewUserCouponRequest;
import com.buka.util.JsonData;

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
