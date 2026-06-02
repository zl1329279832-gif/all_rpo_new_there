package com.medical.device.statemachine;

import com.medical.device.enums.DeviceStatus;
import com.medical.device.enums.QcResult;
import com.medical.device.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class DeviceStateMachine {

    private final Map<DeviceStatus, Set<DeviceStatus>> transitionMap = new HashMap<>();

    public DeviceStateMachine() {
        transitionMap.put(DeviceStatus.NORMAL, 
            Set.of(DeviceStatus.IN_MAINTENANCE, DeviceStatus.IN_REPAIR, DeviceStatus.IN_CALIBRATION, DeviceStatus.IDLE, DeviceStatus.SCRAPPED));
        
        transitionMap.put(DeviceStatus.IN_MAINTENANCE,
            Set.of(DeviceStatus.NORMAL, DeviceStatus.IN_REPAIR, DeviceStatus.SCRAPPED));
        
        transitionMap.put(DeviceStatus.IN_REPAIR,
            Set.of(DeviceStatus.NORMAL, DeviceStatus.IN_MAINTENANCE, DeviceStatus.SCRAPPED));
        
        transitionMap.put(DeviceStatus.IN_CALIBRATION,
            Set.of(DeviceStatus.NORMAL, DeviceStatus.IN_REPAIR, DeviceStatus.SCRAPPED));
        
        transitionMap.put(DeviceStatus.IDLE,
            Set.of(DeviceStatus.NORMAL, DeviceStatus.IN_MAINTENANCE, DeviceStatus.IN_REPAIR, DeviceStatus.SCRAPPED));
        
        transitionMap.put(DeviceStatus.SCRAPPED, Set.of());
    }

    public boolean canTransition(DeviceStatus current, DeviceStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return transitionMap.getOrDefault(current, Set.of()).contains(target);
    }

    public DeviceStatus transition(DeviceStatus current, DeviceStatus target, Integer qcStatus) {
        if (!canTransition(current, target)) {
            throw new BusinessException("设备状态不允许从 " + current.getDescription() + " 转换为 " + target.getDescription());
        }
        
        if (target == DeviceStatus.NORMAL && qcStatus != null && qcStatus == 2) {
            throw new BusinessException("质控不合格的设备禁止标记为正常使用");
        }
        
        log.info("设备状态转换: {} -> {}", current.getDescription(), target.getDescription());
        return target;
    }

    public DeviceStatus startMaintenance(DeviceStatus current) {
        return transition(current, DeviceStatus.IN_MAINTENANCE, null);
    }

    public DeviceStatus startRepair(DeviceStatus current) {
        return transition(current, DeviceStatus.IN_REPAIR, null);
    }

    public DeviceStatus startCalibration(DeviceStatus current) {
        return transition(current, DeviceStatus.IN_CALIBRATION, null);
    }

    public DeviceStatus completeMaintenance(DeviceStatus current, Integer qcStatus) {
        return transition(current, DeviceStatus.NORMAL, qcStatus);
    }

    public DeviceStatus completeRepair(DeviceStatus current, Integer qcStatus) {
        return transition(current, DeviceStatus.NORMAL, qcStatus);
    }

    public DeviceStatus completeCalibration(DeviceStatus current, Integer qcStatus) {
        return transition(current, DeviceStatus.NORMAL, qcStatus);
    }

    public DeviceStatus scrap(DeviceStatus current) {
        return transition(current, DeviceStatus.SCRAPPED, null);
    }
}
