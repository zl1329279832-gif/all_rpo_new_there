package com.medical.device.statemachine;

import com.medical.device.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class RepairOrderStateMachine {

    private final Map<Integer, Set<Integer>> transitionMap = new HashMap<>();

    public RepairOrderStateMachine() {
        transitionMap.put(1, Set.of(2, 6));
        transitionMap.put(2, Set.of(3, 6));
        transitionMap.put(3, Set.of(4, 6));
        transitionMap.put(4, Set.of(5, 3));
        transitionMap.put(5, Set.of());
        transitionMap.put(6, Set.of());
    }

    public boolean canTransition(Integer current, Integer target) {
        if (current == null || target == null) {
            return false;
        }
        return transitionMap.getOrDefault(current, Set.of()).contains(target);
    }

    public void validateTransition(Integer current, Integer target) {
        if (!canTransition(current, target)) {
            String currentDesc = getStatusDesc(current);
            String targetDesc = getStatusDesc(target);
            throw new BusinessException("工单状态不允许从 " + currentDesc + " 转换为 " + targetDesc);
        }
        log.info("工单状态转换: {} -> {}", getStatusDesc(current), getStatusDesc(target));
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 1 -> "待派单";
            case 2 -> "待维修";
            case 3 -> "维修中";
            case 4 -> "待验收";
            case 5 -> "已完成";
            case 6 -> "已取消";
            default -> "未知(" + status + ")";
        };
    }
}
