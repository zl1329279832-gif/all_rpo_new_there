package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_transfer_detail")
public class StockTransferDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long transferId;

    private Long batchId;

    private Long recipeId;

    private BigDecimal transferQty;

    private LocalDateTime createTime;
}
