package com.medical.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.Device;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.DeviceMapper;
import com.medical.device.statemachine.DeviceStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final DeviceStateMachine deviceStateMachine;
    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(cacheNames = "device", key = "'list:' + #pageNum + ':' + #pageSize + ':' + #keyword + ':' + #status + ':' + #riskLevel + ':' + #deptId")
    public PageResult<Device> listDevices(int pageNum, int pageSize, String keyword,
                                          Integer status, Integer riskLevel, Long deptId) {
        Page<Device> page = new Page<>(pageNum, pageSize);
        IPage<Device> result = deviceMapper.selectPageWithDept(page, keyword, status, riskLevel, deptId);

        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public Device getDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        return device;
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "device", allEntries = true),
        @CacheEvict(cacheNames = "statistics", allEntries = true)
    })
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

    @Caching(evict = {
        @CacheEvict(cacheNames = "device", allEntries = true),
        @CacheEvict(cacheNames = "statistics", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(Device device) {
        Device existing = deviceMapper.selectById(device.getId());
        if (existing == null) {
            throw new BusinessException("设备不存在");
        }

        if (device.getStatus() != null && !device.getStatus().equals(existing.getStatus())) {
            deviceStateMachine.transition(existing.getStatus(), device.getStatus(), existing.getQcStatus());
        }

        deviceMapper.updateById(device);
        updateHighRiskCache();
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "device", allEntries = true),
        @CacheEvict(cacheNames = "statistics", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        deviceMapper.deleteById(id);
        updateHighRiskCache();
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "device", allEntries = true),
        @CacheEvict(cacheNames = "statistics", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void updateQcStatus(Long id, Integer qcStatus) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        if (qcStatus == null || (qcStatus != 1 && qcStatus != 2)) {
            throw new BusinessException("质控状态值不合法");
        }

        if (qcStatus == 2 && device.getStatus() == 1) {
            throw new BusinessException("质控不合格的设备不能标记为正常使用状态，请先处理设备问题");
        }

        if (qcStatus == 2) {
            device.setStatus(2);
            log.warn("设备 {} 质控不合格，自动设置为维护中状态", device.getDeviceCode());
        }

        device.setQcStatus(qcStatus);
        deviceMapper.updateById(device);
        updateHighRiskCache();
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = "device", allEntries = true),
        @CacheEvict(cacheNames = "statistics", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void updateRiskLevel(Long id, Integer riskLevel) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        if (riskLevel == null || (riskLevel < 1 || riskLevel > 4)) {
            throw new BusinessException("风险等级值不合法，范围应为1-4");
        }

        device.setRiskLevel(riskLevel);
        deviceMapper.updateById(device);
        updateHighRiskCache();
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
