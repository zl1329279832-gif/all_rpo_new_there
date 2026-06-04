package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("base_material")
public class BaseMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String materialCode;

    private String materialName;

    private String category;

    private String unit;

    private String spec;

    private Integer shelfLifeDays;

    private Integer warningDays;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
