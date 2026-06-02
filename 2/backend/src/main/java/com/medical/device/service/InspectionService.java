package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.InspectionPlan;
import com.medical.device.entity.InspectionTask;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.InspectionPlanMapper;
import com.medical.device.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InspectionService {

    private final InspectionPlanMapper planMapper;
    private final InspectionTaskMapper taskMapper;

    public PageResult<InspectionPlan> listPlans(int pageNum, int pageSize, String keyword, Integer status) {
        Page<InspectionPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(InspectionPlan::getPlanName, keyword);
        }
        if (status != null) {
            wrapper.eq(InspectionPlan::getStatus, status);
        }
        
        wrapper.orderByDesc(InspectionPlan::getId);
        IPage<InspectionPlan> result = planMapper.selectPage(page, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createPlan(InspectionPlan plan) {
        if (plan.getNextExecutionDate() == null) {
            plan.setNextExecutionDate(plan.getStartDate());
        }
        planMapper.insert(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(InspectionPlan plan) {
        planMapper.updateById(plan);
    }

    public PageResult<InspectionTask> listTasks(int pageNum, int pageSize, String keyword, 
                                                 Integer status, Long deviceId, LocalDate startDate, LocalDate endDate) {
        Page<InspectionTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(InspectionTask::getTaskName, keyword)
                    .or().like(InspectionTask::getTaskCode, keyword));
        }
        if (status != null) {
            wrapper.eq(InspectionTask::getStatus, status);
        }
        if (deviceId != null) {
            wrapper.eq(InspectionTask::getDeviceId, deviceId);
        }
        if (startDate != null) {
            wrapper.ge(InspectionTask::getPlanDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(InspectionTask::getPlanDate, endDate);
        }
        
        wrapper.orderByDesc(InspectionTask::getId);
        IPage<InspectionTask> result = taskMapper.selectPage(page, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public List<InspectionTask> getTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        return taskMapper.selectByDateRange(startDate, endDate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long taskId, Integer checkResult, String abnormalDesc, 
                            String handleSuggestion, Long inspectorId, String inspectorName) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (task.getStatus() == 3 || task.getStatus() == 5) {
            throw new BusinessException("该任务已完成或已取消");
        }

        task.setActualDate(LocalDateTime.now());
        task.setInspectorId(inspectorId);
        task.setInspectorName(inspectorName);
        task.setCheckResult(checkResult);
        task.setAbnormalDescription(abnormalDesc);
        task.setHandleSuggestion(handleSuggestion);
        task.setStatus(3);
        
        taskMapper.updateById(task);
    }

    public InspectionTask getTaskDetail(Long id) {
        return taskMapper.selectWithDevice(id);
    }

    public Map<String, Object> getStatistics() {
        return Map.of(
            "statusStats", taskMapper.countByStatus(),
            "last7Days", taskMapper.countLast7Days()
        );
    }
}
