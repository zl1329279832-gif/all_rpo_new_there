package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("prod_plan")
public class ProdPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private LocalDate planDate;

    private Long storeId;

    private Integer status;

    private String remark;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
