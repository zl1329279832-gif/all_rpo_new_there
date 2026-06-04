package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sales_stat")
public class SalesStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate statDate;

    private Long storeId;

    private Long recipeId;

    private BigDecimal salesQty;

    private BigDecimal salesAmount;

    private BigDecimal costAmount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
