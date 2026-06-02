package com.medical.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 维修工单状态枚举
 */
@Getter
public enum RepairOrderStatus {

    PENDING(1, "待处理"),
    ASSIGNED(2, "已派单"),
    IN_PROGRESS(3, "维修中"),
    PENDING_PARTS(4, "待配件"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    @EnumValue
    private final Integer code;
    private final String desc;

    RepairOrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RepairOrderStatus getByCode(Integer code) {
        for (RepairOrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
