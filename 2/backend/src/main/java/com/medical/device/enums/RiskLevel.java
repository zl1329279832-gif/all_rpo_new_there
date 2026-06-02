package com.medical.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 风险等级枚举
 */
@Getter
public enum RiskLevel {

    LOW(1, "低风险"),
    MEDIUM(2, "中风险"),
    HIGH(3, "高风险"),
    CRITICAL(4, "极高风险");

    @EnumValue
    private final Integer code;
    private final String desc;

    RiskLevel(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RiskLevel getByCode(Integer code) {
        for (RiskLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }
}
