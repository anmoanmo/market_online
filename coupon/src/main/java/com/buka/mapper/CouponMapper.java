package com.buka.mapper;

import com.buka.model.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LZX
 * @since 2025-02-14
 */
public interface CouponMapper extends BaseMapper<CouponDO> {

    @Update("update coupon set stock=stock-1 where id = #{couponId} and stock>0")
    int reduceStock(Long couponId);
}
