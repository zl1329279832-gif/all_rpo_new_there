package com.wms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboundStrategy {

    FIFO(1, "先进先出"),
    FEFO(2, "效期优先"),
    SPECIFY_BATCH(3, "指定批次");

    private final Integer code;
    private final String name;

    public static OutboundStrategy fromCode(Integer code) {
        if (code == null) {
            return FIFO;
        }
        for (OutboundStrategy strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return FIFO;
    }
}
