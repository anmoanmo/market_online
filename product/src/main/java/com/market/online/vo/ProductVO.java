package com.market.online.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *

 */
@Data
public class ProductVO  {


    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 封面图
     */

    private String coverImg;

    /**
     * 详情
     */
    private String detail;

    /**
     * 老价格
     */

    private BigDecimal oldPrice;

    /**
     * 新价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;




}
