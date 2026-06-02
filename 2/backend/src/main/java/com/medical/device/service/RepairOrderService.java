package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.Device;
import com.medical.device.entity.DowntimeRecord;
import com.medical.device.entity.PartReplacement;
import com.medical.device.entity.RepairOrder;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.DeviceMapper;
import com.medical.device.mapper.DowntimeRecordMapper;
import com.medical.device.mapper.PartReplacementMapper;
import com.medical.device.mapper.RepairOrderMapper;
import com.medical.device.statemachine.DeviceStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final DeviceMapper deviceMapper;
    private final PartReplacementMapper partReplacementMapper;
    private final DowntimeRecordMapper downtimeRecordMapper;
    private final DeviceStateMachine deviceStateMachine;

    public PageResult<RepairOrder> listOrders(int pageNum, int pageSize, String keyword,
                                              Integer status, Integer faultLevel, Long deviceId) {
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(RepairOrder::getOrderCode, keyword)
                    .or().like(RepairOrder::getFaultDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (faultLevel != null) {
            wrapper.eq(RepairOrder::getFaultLevel, faultLevel);
        }
        if (deviceId != null) {
            wrapper.eq(RepairOrder::getDeviceId, deviceId);
        }

        wrapper.orderByDesc(RepairOrder::getId);
        IPage<RepairOrder> result = repairOrderMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public RepairOrder getOrder(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public RepairOrder createOrder(RepairOrder order) {
        Device device = deviceMapper.selectById(order.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        order.setOrderCode(generateOrderCode());
        order.setReportTime(LocalDateTime.now());
        order.setStatus(1);
        repairOrderMapper.insert(order);

        deviceStateMachine.transition(device.getStatus(), 3, null);
        device.setStatus(3);
        deviceMapper.updateById(device);

        DowntimeRecord downtime = new DowntimeRecord();
        downtime.setDeviceId(order.getDeviceId());
        downtime.setRepairOrderId(order.getId());
        downtime.setDowntimeType(1);
        downtime.setStartTime(LocalDateTime.now());
        downtime.setReason(order.getFaultDescription());
        downtimeRecordMapper.insert(downtime);

        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignOrder(Long id, Long repairerId, String repairerName) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待派单的工单可以派单");
        }

        order.setRepairerId(repairerId);
        order.setRepairerName(repairerName);
        order.setAssignTime(LocalDateTime.now());
        order.setStatus(2);
        repairOrderMapper.updateById(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startRepair(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("只有待维修的工单可以开始维修");
        }

        order.setStartTime(LocalDateTime.now());
        order.setStatus(3);
        repairOrderMapper.updateById(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id, String repairContent, String repairResult,
                               List<PartReplacement> parts) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("只有维修中的工单可以完成");
        }

        order.setCompleteTime(LocalDateTime.now());
        order.setRepairContent(repairContent);
        order.setRepairResult(repairResult);
        order.setStatus(4);

        if (order.getStartTime() != null && order.getCompleteTime() != null) {
            Duration duration = Duration.between(order.getStartTime(), order.getCompleteTime());
            int downtimeHours = (int) Math.ceil(duration.toMinutes() / 60.0);
            order.setDowntime(downtimeHours);
        }

        repairOrderMapper.updateById(order);

        if (parts != null && !parts.isEmpty()) {
            for (PartReplacement part : parts) {
                part.setRepairOrderId(id);
                partReplacementMapper.insert(part);
            }
        }

        LambdaQueryWrapper<DowntimeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DowntimeRecord::getRepairOrderId, id);
        DowntimeRecord downtimeRecord = downtimeRecordMapper.selectOne(wrapper);
        if (downtimeRecord != null) {
            downtimeRecord.setEndTime(LocalDateTime.now());
            if (downtimeRecord.getStartTime() != null && downtimeRecord.getEndTime() != null) {
                Duration dur = Duration.between(downtimeRecord.getStartTime(), downtimeRecord.getEndTime());
                downtimeRecord.setDuration((int) Math.ceil(dur.toMinutes() / 60.0));
            }
            downtimeRecordMapper.updateById(downtimeRecord);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long id, Integer qcStatus) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (order.getStatus() != 4) {
            throw new BusinessException("只有待验收的工单可以验收");
        }

        order.setStatus(5);
        repairOrderMapper.updateById(order);

        Device device = deviceMapper.selectById(order.getDeviceId());
        if (device != null) {
            Integer currentStatus = device.getStatus();
            Integer targetStatus = 1;
            deviceStateMachine.transition(currentStatus, targetStatus, qcStatus);
            device.setStatus(targetStatus);
            if (order.getDowntime() != null) {
                device.setTotalDowntime(
                    (device.getTotalDowntime() != null ? device.getTotalDowntime() : 0) + order.getDowntime()
                );
            }
            deviceMapper.updateById(device);
        }
    }

    public Map<String, Object> getStatistics() {
        return Map.of(
            "statusStats", repairOrderMapper.countByStatus(),
            "totalDowntime", repairOrderMapper.sumTotalDowntime(),
            "monthlyTrend", repairOrderMapper.countMonthlyTrend()
        );
    }

    private String generateOrderCode() {
        return "RO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" +
               String.format("%04d", System.currentTimeMillis() % 10000);
    }
}
