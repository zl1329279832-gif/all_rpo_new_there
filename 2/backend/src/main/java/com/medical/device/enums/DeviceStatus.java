package com.medical.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 设备状态枚举
 */
@Getter
public enum DeviceStatus {

    NORMAL(1, "正常使用"),
    MAINTENANCE(2, "维护中"),
    REPAIRING(3, "维修中"),
    CALIBRATION(4, "校准中"),
    SCRAPPED(5, "已报废"),
    IDLE(6, "闲置");

    @EnumValue
    private final Integer code;
    private final String desc;

    DeviceStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeviceStatus getByCode(Integer code) {
        for (DeviceStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
