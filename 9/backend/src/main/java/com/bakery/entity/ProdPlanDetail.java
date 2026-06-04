package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prod_plan_detail")
public class ProdPlanDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long recipeId;

    private BigDecimal planQty;

    private BigDecimal actualQty;

    private LocalDateTime createTime;
}
