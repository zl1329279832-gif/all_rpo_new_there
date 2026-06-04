package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prod_batch")
public class ProdBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private Long recipeId;

    private Long planId;

    private Long storeId;

    private BigDecimal produceQty;

    private LocalDateTime produceTime;

    private LocalDateTime expireTime;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
