package com.wms.statemachine;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum InventoryState {

    NORMAL(1, "正常", Arrays.asList(
            InventoryEvent.LOCK,
            InventoryEvent.FREEZE,
            InventoryEvent.EXPIRE,
            InventoryEvent.INBOUND,
            InventoryEvent.OUTBOUND
    )),

    LOCKED(2, "已锁定", Arrays.asList(
            InventoryEvent.UNLOCK,
            InventoryEvent.FREEZE,
            InventoryEvent.OUTBOUND
    )),

    FROZEN(3, "已冻结", Arrays.asList(
            InventoryEvent.UNFREEZE,
            InventoryEvent.EXPIRE
    )),

    EXPIRED(4, "已过期", Arrays.asList(
            InventoryEvent.OUTBOUND,
            InventoryEvent.ADJUST
    )),

    NEAR_EXPIRE(5, "临期", Arrays.asList(
            InventoryEvent.LOCK,
            InventoryEvent.FREEZE,
            InventoryEvent.EXPIRE,
            InventoryEvent.OUTBOUND
    ));

    private final Integer code;
    private final String name;
    private final List<InventoryEvent> allowedEvents;

    public boolean canTransition(InventoryEvent event) {
        return allowedEvents.contains(event);
    }

    public static InventoryState fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InventoryState state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }
}
