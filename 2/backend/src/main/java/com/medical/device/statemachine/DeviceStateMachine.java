package com.medical.device.statemachine;

import com.medical.device.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class DeviceStateMachine {

    private final Map<Integer, Set<Integer>> transitionMap = new HashMap<>();

    public DeviceStateMachine() {
        transitionMap.put(1, Set.of(2, 3, 5, 6, 4));
        transitionMap.put(2, Set.of(1, 3, 4));
        transitionMap.put(3, Set.of(1, 2, 4));
        transitionMap.put(5, Set.of(1, 3, 4));
        transitionMap.put(6, Set.of(1, 2, 3, 4));
        transitionMap.put(4, Set.of());
    }

    public boolean canTransition(Integer current, Integer target) {
        if (current == null || target == null) {
            return false;
        }
        return transitionMap.getOrDefault(current, Set.of()).contains(target);
    }

    public void transition(Integer current, Integer target, Integer qcStatus) {
        if (!canTransition(current, target)) {
            String currentDesc = getStatusDesc(current);
            String targetDesc = getStatusDesc(target);
            throw new BusinessException("设备状态不允许从 " + currentDesc + " 转换为 " + targetDesc);
        }

        if (target == 1 && qcStatus != null && qcStatus == 2) {
            throw new BusinessException("质控不合格的设备禁止标记为正常使用");
        }

        log.info("设备状态转换: {} -> {}", getStatusDesc(current), getStatusDesc(target));
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 1 -> "正常使用";
            case 2 -> "维护中";
            case 3 -> "维修中";
            case 4 -> "已报废";
            case 5 -> "校准中";
            case 6 -> "闲置";
            default -> "未知(" + status + ")";
        };
    }
}
