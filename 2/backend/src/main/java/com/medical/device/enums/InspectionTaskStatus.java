package com.medical.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 巡检任务状态枚举
 */
@Getter
public enum InspectionTaskStatus {

    PENDING(1, "待执行"),
    IN_PROGRESS(2, "执行中"),
    COMPLETED(3, "已完成"),
    EXCEPTION(4, "异常"),
    CANCELLED(5, "已取消");

    @EnumValue
    private final Integer code;
    private final String desc;

    InspectionTaskStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static InspectionTaskStatus getByCode(Integer code) {
        for (InspectionTaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
