package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_transfer")
public class StockTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private Long outStoreId;

    private Long inStoreId;

    private Long recipeId;

    private BigDecimal transferQty;

    private Integer transferType;

    private Integer status;

    private BigDecimal totalQty;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime outboundTime;

    private LocalDateTime inboundTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String fromStoreName;

    @TableField(exist = false)
    private String toStoreName;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String outboundBatchNo;

    @TableField(exist = false)
    private String inboundBatchNo;
}
