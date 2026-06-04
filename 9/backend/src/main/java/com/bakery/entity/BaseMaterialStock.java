package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("base_material_stock")
public class BaseMaterialStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private Long storeId;

    private String batchNo;

    private BigDecimal quantity;

    private LocalDate produceDate;

    private LocalDate expireDate;

    private LocalDateTime inboundTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
