package com.bakery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_store")
public class SysStore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String storeCode;

    private String storeName;

    private Integer storeType;

    private String address;

    private String manager;

    private String phone;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
