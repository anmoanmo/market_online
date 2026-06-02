package com.market.online.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @className: AddressAddDto
 * @author: LZX
 * @date: 2025/2/14 14:25
 * @Version: 1.0
 * @description:
 */
@Data
public class AddressAddDto {
    /**
     * 是否默认收货地址：0->否；1->是
     */
    private Integer defaultStatus;

    /**
     * 收发货人姓名
     */
    @JsonProperty("name")
    private String receiveName;

    /**
     * 收货人电话
     */
    @JsonProperty("mobile")
    private String phone;

    /**
     * 省/直辖市
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    @JsonProperty("district")
    private String region;

    /**
     * 详细地址
     */
    @JsonProperty("address")
    private String detailAddress;


}


