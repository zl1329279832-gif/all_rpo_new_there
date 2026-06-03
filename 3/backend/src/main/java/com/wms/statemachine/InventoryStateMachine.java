package com.wms.statemachine;

import com.wms.common.ResultCode;
import com.wms.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryStateMachine {

    public InventoryState transition(InventoryState currentState, InventoryEvent event) {
        if (currentState == null) {
            throw new BusinessException(ResultCode.DATA_STATUS_ERROR, "当前库存状态为空");
        }

        if (!currentState.canTransition(event)) {
            log.error("状态转换失败: 当前状态[{}] 不允许事件[{}]", currentState.getName(), event.getName());
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR,
                    String.format("当前状态[%s]不允许[%s]操作", currentState.getName(), event.getName()));
        }

        InventoryState targetState = calculateTargetState(currentState, event);
        log.debug("状态转换成功: {} -> {} (事件: {})", currentState.getName(), targetState.getName(), event.getName());

        return targetState;
    }

    private InventoryState calculateTargetState(InventoryState currentState, InventoryEvent event) {
        switch (event) {
            case LOCK:
                return InventoryState.LOCKED;
            case UNLOCK:
                return InventoryState.NORMAL;
            case FREEZE:
                return InventoryState.FROZEN;
            case UNFREEZE:
                return InventoryState.NORMAL;
            case EXPIRE:
                return InventoryState.EXPIRED;
            case INBOUND:
            case OUTBOUND:
            case ADJUST:
            case TRANSFER:
            default:
                return currentState;
        }
    }

    public boolean checkExpire(InventoryState currentState, int remainingDays, int warningDays) {
        if (currentState == InventoryState.EXPIRED) {
            return true;
        }
        if (remainingDays <= 0) {
            return true;
        }
        return remainingDays <= warningDays;
    }

    public InventoryState getExpireState(int remainingDays, int warningDays) {
        if (remainingDays <= 0) {
            return InventoryState.EXPIRED;
        }
        if (remainingDays <= warningDays) {
            return InventoryState.NEAR_EXPIRE;
        }
        return InventoryState.NORMAL;
    }
}
