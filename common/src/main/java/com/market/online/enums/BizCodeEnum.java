package com.market.online.enums;

import lombok.Getter;

/**
 *
 *
 * @Description 状态码定义约束，共6位数，前三位代表服务，后4位代表接口
 *  比如 商品服务210,购物车是220、用户服务230，403代表权限
 *
 **/
public enum  BizCodeEnum {

    /**
     * @description:优惠卷错误
     * @author: LZX
     * @date: 2025/2/15 10:59
     * @param:
     * @return:
     **/
    COUPON_NOT_EXIST(100000, "优惠券不存在"),
    COUPON_NOT_STOCK(100001, "优惠卷发发完"),
    COUPON_GET_FAIL(100002, "优惠券未发布"),
    COUPON_OUT_OF_DATE(100003, "不在领取时间范围之内"),
    COUPON_OUT_OF_LIMIT(100004,"超过领取限制"),
    COUPON_ERROR(100005, "优惠券领取出错"),
    CART_NOT(100006, "购物车中没有该商品"),
    ORDER_ERROR(100007, "创建订单失败"),
    COUPON_LOCK_FAIL(100008, "优惠券锁定失败"),
    COUPON_FAIL(100009, "不符合满减规则"),
    PRICE_FAIL(100010, "与真实价格不符发生错误"),
    COUPON_STATE(100011, "[优惠券微服务]-获取优惠券信息失败"),
    COUPON_NOT_FAIL(100012, "[优惠券微服务]-优惠券状态异常"),
    ORDER_CONFIRM_TOKEN_NOT_EXIST(100013, "防重令牌不存在"),
    ORDER_CONFIRM_TOKEN_EQUAL_FAIL(100014, "重复提交"),
    /**
     * 通用操作码
     */
    OPS_REPEAT(110001,"重复操作"),
    OPS_ERROR(110002,"未知错误"),
    UPLOAD_ERROR(110003,"上传头像失败"),
    Email_ERROR(110004,"邮箱不可为空"),
    /**
     *验证码
     */
    CODE_TO_ERROR(240001,"接收号码不合规"),
    CODE_LIMITED(240002,"验证码发送过快"),
    CODE_ERROR(240003,"验证码错误"),
    CODE_CAPTCHA(240101,"图形验证码错误"),
    DELETE_ADDRESS_ERROR(240110, "请至少保留一个默认地址"),
    STOCK_LOCK_FAIL(240111, "库存扣减失败"),
    PRODUCT_PRICE_FAIL(240112, "[商品微服务]-获取最新购物项和价格失败"),
    USER_ADDR_FAIL(240113, "[用户微服务]-确认收货地址失败"),
    ORDER_PAY_TIME_OUT(240114, "[订单微服务]-支付超时或不存在"),

    /**
     * 账号
     */
    ACCOUNT_REPEAT(250001,"账号已经存在"),
    ACCOUNT_UNREGISTER(250002,"账号不存在"),
    ACCOUNT_PWD_ERROR(250003,"账号或者密码错误"),
    Login_ERROR(222222,"未登录"),
    PERMISSION_DENIED(403001, "无权限访问"),;

    @Getter
    private String message;
    @Getter
    private int code;

    private BizCodeEnum(int code,String message){
        this.code=code;
        this.message=message;
    }
}


