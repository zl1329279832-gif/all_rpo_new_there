package com.medical.device.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 质检结果枚举
 */
@Getter
public enum QcResult {

    PASSED(1, "合格"),
    FAILED(2, "不合格"),
    PENDING(3, "待复检"),
    REPAIRED(4, "维修后合格");

    @EnumValue
    private final Integer code;
    private final String desc;

    QcResult(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static QcResult getByCode(Integer code) {
        for (QcResult result : values()) {
            if (result.getCode().equals(code)) {
                return result;
            }
        }
        return null;
    }
}
