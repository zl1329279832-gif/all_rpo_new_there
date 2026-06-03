package com.medical.device.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.device.entity.MaintenanceContract;
import com.medical.device.mapper.MaintenanceContractMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractReminderScheduler {

    private final MaintenanceContractMapper contractMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExpiringContracts() {
        log.info("开始检查即将到期的维保合同");

        LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
        LambdaQueryWrapper<MaintenanceContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenanceContract::getStatus, 1)
               .le(MaintenanceContract::getEndDate, thirtyDaysLater)
               .ge(MaintenanceContract::getEndDate, LocalDate.now());

        List<MaintenanceContract> contracts = contractMapper.selectList(wrapper);

        for (MaintenanceContract contract : contracts) {
            String key = "contract:reminder:" + contract.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                continue;
            }

            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), contract.getEndDate());
            log.warn("合同即将到期: {}, 剩余 {} 天", contract.getContractName(), daysUntilExpiry);

            if (daysUntilExpiry <= 7) {
                contract.setStatus(2);
                contractMapper.updateById(contract);
            }

            redisTemplate.opsForValue().set(key, daysUntilExpiry, 1, TimeUnit.DAYS);
        }

        log.info("合同到期检查完成，发现 {} 个即将到期的合同", contracts.size());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredContracts() {
        log.info("开始检查已过期的维保合同");

        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<MaintenanceContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenanceContract::getStatus, 1)
               .lt(MaintenanceContract::getEndDate, today);

        List<MaintenanceContract> contracts = contractMapper.selectList(wrapper);
        for (MaintenanceContract contract : contracts) {
            contract.setStatus(3);
            contractMapper.updateById(contract);
            log.warn("合同已过期: {}", contract.getContractName());
        }

        log.info("过期合同检查完成，标记 {} 个已过期合同", contracts.size());
    }
}
