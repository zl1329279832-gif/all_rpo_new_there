package com.medical.device.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.device.entity.InspectionPlan;
import com.medical.device.entity.InspectionTask;
import com.medical.device.mapper.InspectionPlanMapper;
import com.medical.device.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionTaskScheduler {

    private final InspectionPlanMapper inspectionPlanMapper;
    private final InspectionTaskMapper inspectionTaskMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void generateDailyInspectionTasks() {
        log.info("开始生成今日巡检任务");

        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionPlan::getStatus, 1)
               .le(InspectionPlan::getNextExecutionDate, today)
               .or(q -> q.isNull(InspectionPlan::getEndDate))
               .or(q -> q.ge(InspectionPlan::getEndDate, today));

        List<InspectionPlan> plans = inspectionPlanMapper.selectList(wrapper);

        for (InspectionPlan plan : plans) {
            createTaskFromPlan(plan, today);
            updateNextExecutionDate(plan);
        }

        log.info("巡检任务生成完成，共生成 {} 个任务", plans.size());
    }

    private void createTaskFromPlan(InspectionPlan plan, LocalDate planDate) {
        LambdaQueryWrapper<InspectionTask> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(InspectionTask::getPlanId, plan.getId())
                    .eq(InspectionTask::getPlanDate, planDate);
        
        Long count = inspectionTaskMapper.selectCount(checkWrapper);
        if (count > 0) {
            log.warn("计划ID {} 的今日任务已存在，跳过", plan.getId());
            return;
        }

        InspectionTask task = new InspectionTask();
        task.setTaskCode(generateTaskCode(planDate));
        task.setPlanId(plan.getId());
        task.setDeviceId(plan.getDeviceId());
        task.setTaskName(plan.getPlanName() + "-" + planDate.format(DateTimeFormatter.ofPattern("MMdd")));
        task.setTaskType(1);
        task.setPlanDate(planDate);
        task.setInspector(plan.getInspector());
        task.setStatus(1);
        task.setCheckItems(plan.getCheckItems());

        inspectionTaskMapper.insert(task);
        log.info("已创建巡检任务: {}", task.getTaskName());
    }

    private void updateNextExecutionDate(InspectionPlan plan) {
        LocalDate nextDate = calculateNextExecutionDate(plan);
        plan.setNextExecutionDate(nextDate);
        inspectionPlanMapper.updateById(plan);
    }

    private LocalDate calculateNextExecutionDate(InspectionPlan plan) {
        LocalDate current = plan.getNextExecutionDate() != null ? 
            plan.getNextExecutionDate() : plan.getStartDate();
        
        return switch (plan.getCycleType()) {
            case 1 -> current.plusDays(1);
            case 2 -> current.plusWeeks(1);
            case 3 -> current.plusMonths(1);
            case 4 -> current.plusMonths(3);
            case 5 -> current.plusYears(1);
            default -> current.plusDays(plan.getCycleDays() != null ? plan.getCycleDays() : 7);
        };
    }

    private String generateTaskCode(LocalDate date) {
        return "IT-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + 
               String.format("%04d", System.currentTimeMillis() % 10000);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkOverdueTasks() {
        log.info("开始检查逾期巡检任务");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionTask::getStatus, 1)
               .lt(InspectionTask::getPlanDate, yesterday);

        List<InspectionTask> tasks = inspectionTaskMapper.selectList(wrapper);
        for (InspectionTask task : tasks) {
            task.setStatus(4);
            inspectionTaskMapper.updateById(task);
        }

        log.info("巡检任务逾期检查完成，标记逾期任务 {} 个", tasks.size());
    }
}
