package com.buka.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buka.enums.BizCodeEnum;
import com.buka.enums.CouponPublishEnum;
import com.buka.enums.CouponStateEnum;
import com.buka.exceptions.BizException;
import com.buka.interceptor.LoginInterceptor;
import com.buka.model.CouponDO;
import com.buka.mapper.CouponMapper;
import com.buka.model.CouponPage;
import com.buka.model.CouponRecordDO;
import com.buka.service.CouponRecordService;
import com.buka.service.CouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buka.util.CommonUtil;
import com.buka.util.JsonData;
import com.buka.vo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LZX
 * @since 2025-02-14
 */

/**
 * @description:分页查询
 * @author: LZX
 * @date: 2025/2/15 10:11
 * @param:
 * @return:
 **/
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements CouponService {
    @Autowired
    private CouponRecordService couponRecordService;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public JsonData pageCoupon(Long page, Long size) {
        //创建分页
        Page<CouponDO> page1 = new Page<>(page, size);
        //写表达式限制优惠卷条件
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        //限制是发布的优惠卷条件
        wrapper.eq(CouponDO::getPublish, CouponPublishEnum.PUBLISH);
        //根据发布时间升序排序
        wrapper.orderByDesc(CouponDO::getCreateTime);
        //分页查询
        this.page(page1, wrapper);
        //获取结果集
        List<CouponDO> couponDOList = page1.getRecords();
        //获取总条数和总页数
        long pages = page1.getPages();
        long total = page1.getTotal();
        //将三条数据封装成一起返回
        CouponPage<CouponDO> couponPage = new CouponPage<>();
        couponPage.setRecords(couponDOList);
        couponPage.setTotal(total);
        couponPage.setPages(pages);
        return JsonData.buildSuccess(couponPage);
    }

//    /**
//     * @description:领取优惠卷
//     * @author: LZX
//     * @date: 2025/2/15 10:36
//     * @param: [id]
//     * @return: com.buka.util.JsonData
//     **/
//    @Override
//    public JsonData addPromotion(Long id) {
//        //通过往redis添加数据实现加锁，避免超领问题
//        //设置key和val
//        String key = "lock:coupon:" + id;
//        String val = CommonUtil.generateUUID();
//        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, val, Duration.ofSeconds(30));
//        if (ifAbsent) {
//            try {
//                log.info("成功加锁");
//                //获取当前登录的用户
//                LoginUser loginUser = LoginInterceptor.threadLocal.get();
//                //查询优惠卷
//                CouponDO couponDO = this.getById(id);
//                //判断优惠券是否可用
//                this.checkCoupon(couponDO, loginUser.getId());
//                //进行修改数据库
//                int count = couponMapper.reduceStock(couponDO.getId());
//                //插入领取记录
//                if (count == 1) {
//                    CouponRecordDO couponRecordDO = new CouponRecordDO();
//                    BeanUtils.copyProperties(couponDO, couponRecordDO);
//                    couponRecordDO.setCreateTime(new Date());
//                    couponRecordDO.setUserId(loginUser.getId());
//                    couponRecordDO.setUserName(loginUser.getName());
//                    couponRecordDO.setCouponId(couponDO.getId());
//                    couponRecordDO.setUseState(CouponStateEnum.NEW.name());
//                    couponRecordService.save(couponRecordDO);
//                }
//
//            } catch (Exception e) {
//
//            } finally {
//                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return redis.call('set',KEYS[1]) end";
//                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), val);
//                log.info("释放锁");
//            }
//        } else {
//            //加锁失败
//            try {
//                TimeUnit.MICROSECONDS.sleep(100L);
//            } catch (Exception e) {
//            } finally {
//                return addPromotion(id);
//            }
//        }
//
//        return JsonData.buildSuccess();
//    }
    /**
     * @description:领取优惠卷
     * @author: LZX
     * @date: 2025/2/15 10:36
     * @param: [id]
     * @return: com.buka.util.JsonData
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData addPromotion(Long id) {
        //通过往redis添加数据实现加锁，避免超领问题
        //设置key和val
        RLock lock = redissonClient.getLock("lock:coupon:" + id);
        lock.lock();
        try {
            log.info("成功加锁");
            //获取当前登录的用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            //查询优惠卷
            CouponDO couponDO = this.getById(id);
            //判断优惠券是否可用
            this.checkCoupon(couponDO, loginUser.getId());
            //进行修改数据库
            int count = couponMapper.reduceStock(couponDO.getId());
            //插入领取记录
            if (count == 1) {
                CouponRecordDO couponRecordDO = new CouponRecordDO();
                BeanUtils.copyProperties(couponDO, couponRecordDO);
                couponRecordDO.setCreateTime(new Date());
                couponRecordDO.setUserId(loginUser.getId());
                couponRecordDO.setUserName(loginUser.getName());
                couponRecordDO.setCouponId(couponDO.getId());
                couponRecordDO.setUseState(CouponStateEnum.NEW.name());
                couponRecordService.save(couponRecordDO);
            }

        } catch (Exception e) {
            throw e;
        } finally {
            lock.unlock();
        }
        return JsonData.buildSuccess();
    }

    /**
     * @description:判断优惠券是否可用
     * @author: LZX
     * @date: 2025/2/15 10:40
     * @param: [couponDO, userId]
     * @return: void
     **/
    public void checkCoupon(CouponDO couponDO, Long userId) {
        //判断优惠券是否存在
        if (couponDO == null) {
            throw new BizException(BizCodeEnum.COUPON_NOT_EXIST);
        }
        //判断优惠券是否已发布
        if (!couponDO.getPublish().equals(CouponPublishEnum.PUBLISH.name())) {
            throw new BizException(BizCodeEnum.COUPON_GET_FAIL);
        }
        //判断优惠券库存是否富裕
        if (couponDO.getStock() <= 0) {
            throw new BizException(BizCodeEnum.COUPON_NOT_STOCK);
        }
        //判断优惠券是否过期
        //将优惠券时间转为时间戳
        long start = couponDO.getStartTime().getTime();
        long end = couponDO.getEndTime().getTime();
        //获取当前时间
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < start || currentTimeMillis > end) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_DATE);
        }
        //判断当前用户是否已领取过该优惠券
        LambdaQueryWrapper<CouponRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponRecordDO::getUserId, userId);
        wrapper.eq(CouponRecordDO::getCouponId, couponDO.getId());
        //查询领取数量
        int i = couponRecordService.count(wrapper);
        if (i >= couponDO.getUserLimit()) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_LIMIT);
        }

    }

}
