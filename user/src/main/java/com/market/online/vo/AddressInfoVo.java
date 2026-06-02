package com.market.online.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @className: AddressInfoVo
 * @author: LZX
 * @date: 2025/2/14 15:24
 * @Version: 1.0
 * @description:
 */
@Data
public class AddressInfoVo {
    private Long id;

    private Integer defaultStatus;

    @JsonProperty("name")
    private String receiveName;

    @JsonProperty("mobile")
    private String phone;

    private String province;

    private String city;

    @JsonProperty("district")
    private String region;

    @JsonProperty("address")
    private String detailAddress;


}


