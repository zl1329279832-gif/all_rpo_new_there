package com.wms.statemachine;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventoryEvent {

    INBOUND(1, "入库"),
    OUTBOUND(2, "出库"),
    LOCK(3, "锁定"),
    UNLOCK(4, "解锁"),
    FREEZE(5, "冻结"),
    UNFREEZE(6, "解冻"),
    EXPIRE(7, "过期"),
    ADJUST(8, "调整"),
    TRANSFER(9, "调拨");

    private final Integer code;
    private final String name;

    public static InventoryEvent fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InventoryEvent event : values()) {
            if (event.getCode().equals(code)) {
                return event;
            }
        }
        return null;
    }
}
