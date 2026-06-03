package com.medical.device.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.device.entity.InspectionPlan;
import com.medical.device.entity.InspectionTask;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.InspectionPlanMapper;
import com.medical.device.mapper.InspectionTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionTaskScheduler {

    private final InspectionPlanMapper inspectionPlanMapper;
    private final InspectionTaskMapper inspectionTaskMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TASK_GENERATE_LOCK = "schedule:inspection:generate:lock";
    private static final String TASK_OVERDUE_LOCK = "schedule:inspection:overdue:lock";
    private static final String TASK_CODE_SEQUENCE = "inspection:task:code:sequence:";
    private static final AtomicInteger taskCounter = new AtomicInteger(0);

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void generateDailyInspectionTasks() {
        String lockKey = TASK_GENERATE_LOCK + ":" + LocalDate.now();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 2, TimeUnit.HOURS);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("其他实例正在生成巡检任务，跳过执行");
            return;
        }

        try {
            log.info("开始生成今日巡检任务");
            taskCounter.set(0);

            LocalDate today = LocalDate.now();
            LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InspectionPlan::getStatus, 1)
                   .le(InspectionPlan::getNextExecutionDate, today);

            List<InspectionPlan> plans = inspectionPlanMapper.selectList(wrapper);

            for (InspectionPlan plan : plans) {
                if (plan.getEndDate() != null && today.isAfter(plan.getEndDate())) {
                    log.info("计划ID {} 已超过结束日期，跳过", plan.getId());
                    continue;
                }
                createTaskFromPlan(plan, today);
                updateNextExecutionDate(plan);
            }

            log.info("巡检任务生成完成，共生成 {} 个任务", taskCounter.get());
        } catch (Exception e) {
            log.error("生成巡检任务失败", e);
            throw new BusinessException("生成巡检任务失败: " + e.getMessage());
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private void createTaskFromPlan(InspectionPlan plan, LocalDate planDate) {
        LambdaQueryWrapper<InspectionTask> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(InspectionTask::getPlanId, plan.getId())
                    .eq(InspectionTask::getPlanDate, planDate);

        Long count = inspectionTaskMapper.selectCount(checkWrapper);
        if (count > 0) {
            log.warn("计划ID {} 在日期 {} 的任务已存在，跳过", plan.getId(), planDate);
            return;
        }

        InspectionTask task = new InspectionTask();
        task.setTaskCode(generateTaskCode(planDate));
        task.setPlanId(plan.getId());
        task.setDeviceId(plan.getDeviceId());
        task.setTaskName(plan.getPlanName() + "-" + planDate.format(DateTimeFormatter.ofPattern("MMdd")));
        task.setTaskType(1);
        task.setPlanDate(planDate);
        task.setStatus(1);
        task.setInspectorName(plan.getInspector());

        inspectionTaskMapper.insert(task);
        taskCounter.incrementAndGet();
        log.info("已创建巡检任务: {}", task.getTaskName());
    }

    private void updateNextExecutionDate(InspectionPlan plan) {
        LocalDate nextDate = calculateNextExecutionDate(plan);
        if (plan.getEndDate() != null && nextDate.isAfter(plan.getEndDate())) {
            log.info("计划ID {} 下次执行日期已超过结束日期，标记为禁用", plan.getId());
            plan.setStatus(0);
        } else {
            plan.setNextExecutionDate(nextDate);
        }
        inspectionPlanMapper.updateById(plan);
    }

    private LocalDate calculateNextExecutionDate(InspectionPlan plan) {
        LocalDate current = plan.getNextExecutionDate() != null ?
            plan.getNextExecutionDate() : plan.getStartDate();

        LocalDate nextDate = switch (plan.getCycleType()) {
            case 1 -> current.plusDays(1);
            case 2 -> current.plusWeeks(1);
            case 3 -> current.plusMonths(1);
            case 4 -> current.plusMonths(3);
            case 5 -> current.plusYears(1);
            default -> current.plusDays(plan.getCycleDays() != null ? plan.getCycleDays() : 7);
        };

        LocalDate today = LocalDate.now();
        while (!nextDate.isAfter(today)) {
            nextDate = switch (plan.getCycleType()) {
                case 1 -> nextDate.plusDays(1);
                case 2 -> nextDate.plusWeeks(1);
                case 3 -> nextDate.plusMonths(1);
                case 4 -> nextDate.plusMonths(3);
                case 5 -> nextDate.plusYears(1);
                default -> nextDate.plusDays(plan.getCycleDays() != null ? plan.getCycleDays() : 7);
            };
        }

        return nextDate;
    }

    private String generateTaskCode(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqKey = TASK_CODE_SEQUENCE + dateStr;
        
        Long sequence = redisTemplate.opsForValue().increment(seqKey);
        if (sequence == null || sequence == 1) {
            redisTemplate.expire(seqKey, 48, TimeUnit.HOURS);
        }
        
        return "IT-" + dateStr + "-" + String.format("%04d", sequence % 10000);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkOverdueTasks() {
        String lockKey = TASK_OVERDUE_LOCK + ":" + LocalDate.now();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 1, TimeUnit.HOURS);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("其他实例正在检查逾期任务，跳过执行");
            return;
        }

        try {
            log.info("开始检查逾期巡检任务");

            LocalDate today = LocalDate.now();
            LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InspectionTask::getStatus, 1)
                   .lt(InspectionTask::getPlanDate, today);

            List<InspectionTask> tasks = inspectionTaskMapper.selectList(wrapper);
            for (InspectionTask task : tasks) {
                task.setStatus(4);
                inspectionTaskMapper.updateById(task);
                log.warn("巡检任务已逾期: {}", task.getTaskCode());
            }

            log.info("巡检任务逾期检查完成，标记逾期任务 {} 个", tasks.size());
        } catch (Exception e) {
            log.error("检查逾期任务失败", e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
