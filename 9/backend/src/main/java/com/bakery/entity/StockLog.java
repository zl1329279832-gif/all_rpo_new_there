package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_log")
public class StockLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long storeId;

    private String bizType;

    private String bizNo;

    private Long batchId;

    private Long recipeId;

    private Long materialId;

    private BigDecimal qtyBefore;

    private BigDecimal qtyChange;

    private BigDecimal qtyAfter;

    private String operator;

    private LocalDateTime createTime;
}
