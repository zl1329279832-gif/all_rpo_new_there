package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("base_recipe_detail")
public class BaseRecipeDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recipeId;

    private Long materialId;

    private BigDecimal dosage;

    private LocalDateTime createTime;
}
