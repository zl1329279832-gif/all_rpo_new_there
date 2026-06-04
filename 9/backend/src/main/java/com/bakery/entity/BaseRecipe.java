package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("base_recipe")
public class BaseRecipe {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productCode;

    private String productName;

    private String category;

    private String unit;

    private Integer shelfLifeHours;

    private Integer warningHours;

    private BigDecimal outputQty;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
