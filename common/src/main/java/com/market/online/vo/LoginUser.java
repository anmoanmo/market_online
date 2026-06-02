package com.market.online.vo;

import lombok.Data;

/**
 * @className: LoginUser
 * @author: LZX
 * @date: 2025/2/14 10:15
 * @Version: 1.0
 * @description:
 */
@Data
public class LoginUser {
    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 头像
     */

    private String headImg;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 是否管理员 0普通 1管理
     */
    private Integer admin;
}


