package com.buka.vo;

import lombok.Data;

/**
 * @className: UserInfoVo
 * @author: LZX
 * @date: 2025/2/14 14:19
 * @Version: 1.0
 * @description:
 */
@Data
public class UserInfoVo {
    private Long id;

    /**
     * 昵称
     */
    private String name;


    /**
     * 头像
     */
    private String headImg;

    /**
     * 用户签名
     */
    private String slogan;

    /**
     * 0表示女，1表示男
     */
    private Integer sex;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 是否管理员
     */
    private Integer admin;
}


