package com.market.online.dto;

import lombok.Data;

/**
 * @className: UserRegDto
 * @author: LZX
 * @date: 2025/2/13 15:25
 * @Version: 1.0
 * @description:接受前端传入的json数据
 */
@Data
public class UserRegDto {
    private String name;
    private String pwd;
    private String headImg;
    private String mail;
    private String code;
}


