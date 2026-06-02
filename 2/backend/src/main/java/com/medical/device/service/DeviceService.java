package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.Device;
import com.medical.device.enums.DeviceStatus;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.DeviceMapper;
import com.medical.device.statemachine.DeviceStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final DeviceStateMachine deviceStateMachine;
    private final RedisTemplate<String, Object> redisTemplate;

    public PageResult<Device> listDevices(int pageNum, int pageSize, String keyword, 
                                          Integer status, Integer riskLevel, Long deptId) {
        Page<Device> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Device::getDeviceName, keyword)
                    .or().like(Device::getDeviceCode, keyword)
                    .or().like(Device::getManufacturer, keyword));
        }
        if (status != null) {
            wrapper.eq(Device::getStatus, status);
        }
        if (riskLevel != null) {
            wrapper.eq(Device::getRiskLevel, riskLevel);
        }
        if (deptId != null) {
            wrapper.eq(Device::getDeptId, deptId);
        }
        
        wrapper.orderByDesc(Device::getId);
        IPage<Device> result = deviceMapper.selectPage(page, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public Device getDevice(Long id) {
        Device device = deviceMapper.selectDeviceWithDept(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        return device;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createDevice(Device device) {
        if (device.getStatus() == null) {
            device.setStatus(1);
        }
        if (device.getQcStatus() == null) {
            device.setQcStatus(1);
        }
        deviceMapper.insert(device);
        updateHighRiskCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(Device device) {
        Device existing = deviceMapper.selectById(device.getId());
        if (existing == null) {
            throw new BusinessException("设备不存在");
        }
        
        if (device.getStatus() != null && !device.getStatus().equals(existing.getStatus())) {
            DeviceStatus currentStatus = DeviceStatus.fromCode(existing.getStatus());
            DeviceStatus targetStatus = DeviceStatus.fromCode(device.getStatus());
            deviceStateMachine.transition(currentStatus, targetStatus, existing.getQcStatus());
        }
        
        deviceMapper.updateById(device);
        updateHighRiskCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        deviceMapper.deleteById(id);
        updateHighRiskCache();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateQcStatus(Long id, Integer qcStatus) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        
        device.setQcStatus(qcStatus);
        deviceMapper.updateById(device);
        
        if (qcStatus == 2 && device.getStatus() == 1) {
            throw new BusinessException("质控不合格，设备将不能标记为正常使用状态");
        }
    }

    public List<Device> getHighRiskDevices() {
        return deviceMapper.selectHighRiskDevices();
    }

    public Map<String, Object> getDeviceStatistics() {
        return Map.of(
            "statusStats", deviceMapper.countByStatus(),
            "riskLevelStats", deviceMapper.countByRiskLevel(),
            "deptStats", deviceMapper.countByDept(),
            "highRiskCount", deviceMapper.countHighRiskDevices()
        );
    }

    private void updateHighRiskCache() {
        Long count = deviceMapper.countHighRiskDevices();
        redisTemplate.opsForValue().set("device:high_risk:count", count);
    }
}
