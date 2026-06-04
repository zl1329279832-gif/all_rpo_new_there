package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_damage_detail")
public class StockDamageDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long damageId;

    private Long batchId;

    private Long recipeId;

    private BigDecimal damageQty;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private LocalDateTime createTime;
}
