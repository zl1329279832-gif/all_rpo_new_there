package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prod_batch")
public class ProdBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private Long recipeId;

    private Long planId;

    private Long storeId;

    private BigDecimal produceQty;

    private LocalDateTime produceTime;

    private LocalDateTime expireTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productCode;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String category;

    @TableField(exist = false)
    private String storeName;

    @TableField(exist = false)
    private Integer warningHours;

    @TableField(exist = false)
    private String planNo;

    @TableField(exist = false)
    private BigDecimal totalQty;

    @TableField(exist = false)
    private BigDecimal remainQty;

    @TableField(exist = false)
    private BigDecimal outboundQty;

    @TableField(exist = false)
    private BigDecimal damageQty;

    @TableField(exist = false)
    private String warningLevel;

    @TableField(exist = false)
    private String remainTimeDesc;
}
