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
import com.medical.device.statemachine.RepairOrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final DeviceMapper deviceMapper;
    private final PartReplacementMapper partReplacementMapper;
    private final DowntimeRecordMapper downtimeRecordMapper;
    private final DeviceStateMachine deviceStateMachine;
    private final RepairOrderStateMachine repairOrderStateMachine;
    private final PartReplacementService partReplacementService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_CODE_SEQUENCE = "repair:order:code:sequence:";

    public PageResult<RepairOrder> listOrders(int pageNum, int pageSize, String keyword,
                                              Integer status, Integer faultLevel, Long deviceId) {
        Page<RepairOrder> page = new Page<>(pageNum, pageSize);
        IPage<RepairOrder> result = repairOrderMapper.selectPageWithDevice(page, keyword, status, faultLevel, deviceId);

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

        if (order.getFaultDescription() == null || order.getFaultDescription().trim().isEmpty()) {
            throw new BusinessException("故障描述不能为空");
        }

        if (device.getStatus() == 3) {
            throw new BusinessException("该设备当前已有维修中的工单，请先完成现有维修");
        }

        order.setOrderCode(generateOrderCode());
        order.setReportTime(LocalDateTime.now());
        order.setStatus(1);
        if (order.getFaultLevel() == null) {
            order.setFaultLevel(2);
        }
        repairOrderMapper.insert(order);

        deviceStateMachine.transition(device.getStatus(), 3, device.getQcStatus());
        device.setStatus(3);
        deviceMapper.updateById(device);

        DowntimeRecord downtime = new DowntimeRecord();
        downtime.setDeviceId(order.getDeviceId());
        downtime.setRepairOrderId(order.getId());
        downtime.setDowntimeType(1);
        downtime.setStartTime(LocalDateTime.now());
        downtime.setReason(order.getFaultDescription());
        downtimeRecordMapper.insert(downtime);

        log.info("创建维修工单成功: {}, 设备: {}", order.getOrderCode(), device.getDeviceName());
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignOrder(Long id, Long repairerId, String repairerName) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (repairerId == null || repairerName == null || repairerName.trim().isEmpty()) {
            throw new BusinessException("维修人员信息不完整");
        }

        repairOrderStateMachine.validateTransition(order.getStatus(), 2);

        order.setRepairerId(repairerId);
        order.setRepairerName(repairerName);
        order.setAssignTime(LocalDateTime.now());
        order.setStatus(2);
        repairOrderMapper.updateById(order);
        log.info("工单 {} 已派单给维修人员: {}", order.getOrderCode(), repairerName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startRepair(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        repairOrderStateMachine.validateTransition(order.getStatus(), 3);

        order.setStartTime(LocalDateTime.now());
        order.setStatus(3);
        repairOrderMapper.updateById(order);
        log.info("工单 {} 开始维修", order.getOrderCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id, String repairContent, String repairResult,
                               List<PartReplacement> parts) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (repairContent == null || repairContent.trim().isEmpty()) {
            throw new BusinessException("维修内容不能为空");
        }
        if (repairResult == null || repairResult.trim().isEmpty()) {
            throw new BusinessException("维修结果不能为空");
        }

        repairOrderStateMachine.validateTransition(order.getStatus(), 4);

        LocalDateTime completeTime = LocalDateTime.now();
        order.setCompleteTime(completeTime);
        order.setRepairContent(repairContent);
        order.setRepairResult(repairResult);
        order.setStatus(4);

        if (order.getStartTime() != null) {
            Duration duration = Duration.between(order.getStartTime(), completeTime);
            long minutes = duration.toMinutes();
            int downtimeHours = minutes > 0 ? (int) Math.ceil(minutes / 60.0) : 1;
            order.setDowntime(downtimeHours);
            log.info("工单 {} 停机时间计算: {} 分钟 = {} 小时", order.getOrderCode(), minutes, downtimeHours);
        } else {
            order.setDowntime(1);
            log.warn("工单 {} 缺少开始时间，默认停机1小时", order.getOrderCode());
        }

        BigDecimal totalPartCost = BigDecimal.ZERO;
        if (parts != null && !parts.isEmpty()) {
            for (PartReplacement part : parts) {
                part.setRepairOrderId(id);
                if (part.getReplaceTime() == null) {
                    part.setReplaceTime(completeTime);
                }
                if (part.getUnitPrice() != null && part.getQuantity() != null) {
                    part.setTotalPrice(part.getUnitPrice().multiply(BigDecimal.valueOf(part.getQuantity())));
                }
                partReplacementMapper.insert(part);
                if (part.getTotalPrice() != null) {
                    totalPartCost = totalPartCost.add(part.getTotalPrice());
                }
            }
        }
        order.setRepairCost(totalPartCost);

        repairOrderMapper.updateById(order);

        LambdaQueryWrapper<DowntimeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DowntimeRecord::getRepairOrderId, id);
        DowntimeRecord downtimeRecord = downtimeRecordMapper.selectOne(wrapper);
        if (downtimeRecord != null) {
            downtimeRecord.setEndTime(completeTime);
            if (downtimeRecord.getStartTime() != null) {
                Duration dur = Duration.between(downtimeRecord.getStartTime(), completeTime);
                long minutes = dur.toMinutes();
                int duration = minutes > 0 ? (int) Math.ceil(minutes / 60.0) : 1;
                downtimeRecord.setDuration(duration);
                log.info("停机记录 {} 时间计算: {} 分钟 = {} 小时", downtimeRecord.getId(), minutes, duration);
            }
            downtimeRecordMapper.updateById(downtimeRecord);
        }

        log.info("工单 {} 完成维修，停机时间: {} 小时，配件费用: {}", 
            order.getOrderCode(), order.getDowntime(), totalPartCost);
    }

    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long id, Integer qcStatus) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        repairOrderStateMachine.validateTransition(order.getStatus(), 5);

        order.setStatus(5);
        repairOrderMapper.updateById(order);

        Device device = deviceMapper.selectById(order.getDeviceId());
        if (device != null) {
            Integer currentStatus = device.getStatus();
            Integer targetStatus = 1;
            
            if (qcStatus == null) {
                qcStatus = device.getQcStatus();
            }
            
            deviceStateMachine.transition(currentStatus, targetStatus, qcStatus);
            device.setStatus(targetStatus);
            device.setQcStatus(qcStatus);
            
            if (order.getDowntime() != null) {
                int currentDowntime = device.getTotalDowntime() != null ? device.getTotalDowntime() : 0;
                device.setTotalDowntime(currentDowntime + order.getDowntime());
                log.info("设备 {} 累计停机时间更新: {} + {} = {} 小时", 
                    device.getDeviceCode(), currentDowntime, order.getDowntime(), device.getTotalDowntime());
            }
            device.setLastMaintenanceDate(LocalDate.now());
            deviceMapper.updateById(device);
        }

        log.info("工单 {} 已验收完成", order.getOrderCode());
    }

    public Map<String, Object> getStatistics() {
        return Map.of(
            "statusStats", repairOrderMapper.countByStatus(),
            "totalDowntime", repairOrderMapper.sumTotalDowntime(),
            "monthlyTrend", repairOrderMapper.countMonthlyTrend()
        );
    }

    private String generateOrderCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqKey = ORDER_CODE_SEQUENCE + dateStr;
        
        Long sequence = redisTemplate.opsForValue().increment(seqKey);
        if (sequence == null || sequence == 1) {
            redisTemplate.expire(seqKey, 48, TimeUnit.HOURS);
        }
        
        return "RO-" + dateStr + "-" + String.format("%04d", sequence % 10000);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }

        repairOrderStateMachine.validateTransition(order.getStatus(), 6);

        order.setStatus(6);
        repairOrderMapper.updateById(order);

        Device device = deviceMapper.selectById(order.getDeviceId());
        if (device != null && device.getStatus() == 3) {
            device.setStatus(1);
            deviceMapper.updateById(device);
        }

        LambdaQueryWrapper<DowntimeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DowntimeRecord::getRepairOrderId, id);
        DowntimeRecord downtimeRecord = downtimeRecordMapper.selectOne(wrapper);
        if (downtimeRecord != null && downtimeRecord.getEndTime() == null) {
            downtimeRecord.setEndTime(LocalDateTime.now());
            Duration dur = Duration.between(downtimeRecord.getStartTime(), downtimeRecord.getEndTime());
            long minutes = dur.toMinutes();
            int duration = minutes > 0 ? (int) Math.ceil(minutes / 60.0) : 1;
            downtimeRecord.setDuration(duration);
            downtimeRecordMapper.updateById(downtimeRecord);
        }

        log.info("工单 {} 已取消", order.getOrderCode());
    }
}
