package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_damage")
public class StockDamage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String damageNo;

    private Long storeId;

    private Long batchId;

    private Integer damageType;

    private Integer status;

    private BigDecimal damageQty;

    private BigDecimal damageAmount;

    private BigDecimal totalQty;

    private BigDecimal totalAmount;

    private String reason;

    private Long createBy;

    private Long auditBy;

    private LocalDateTime createTime;

    private LocalDateTime auditTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String storeName;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String batchNo;

    @TableField(exist = false)
    private String auditRemark;
}
