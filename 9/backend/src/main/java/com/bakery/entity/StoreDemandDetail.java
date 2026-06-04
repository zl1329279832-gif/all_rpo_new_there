package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("store_demand_detail")
public class StoreDemandDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long demandId;

    private Long recipeId;

    private BigDecimal demandQty;

    private BigDecimal confirmQty;

    private BigDecimal deliverQty;

    private LocalDateTime createTime;
}
