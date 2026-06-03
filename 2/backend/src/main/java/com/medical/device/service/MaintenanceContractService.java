package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.Device;
import com.medical.device.entity.MaintenanceContract;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.DeviceMapper;
import com.medical.device.mapper.MaintenanceContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceContractService {

    private final MaintenanceContractMapper maintenanceContractMapper;
    private final DeviceMapper deviceMapper;

    public PageResult<MaintenanceContract> listContracts(int pageNum, int pageSize, String keyword,
                                                         Integer status, Integer contractType, Long deviceId) {
        Page<MaintenanceContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MaintenanceContract> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(MaintenanceContract::getContractCode, keyword)
                    .or().like(MaintenanceContract::getContractName, keyword)
                    .or().like(MaintenanceContract::getSupplier, keyword));
        }
        if (status != null) {
            wrapper.eq(MaintenanceContract::getStatus, status);
        }
        if (contractType != null) {
            wrapper.eq(MaintenanceContract::getContractType, contractType);
        }
        if (deviceId != null) {
            wrapper.eq(MaintenanceContract::getDeviceId, deviceId);
        }

        wrapper.orderByDesc(MaintenanceContract::getId);
        IPage<MaintenanceContract> result = maintenanceContractMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public MaintenanceContract getContract(Long id) {
        MaintenanceContract contract = maintenanceContractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("维保合同不存在");
        }
        return contract;
    }

    @Transactional(rollbackFor = Exception.class)
    public MaintenanceContract createContract(MaintenanceContract contract) {
        if (contract.getDeviceId() != null) {
            Device device = deviceMapper.selectById(contract.getDeviceId());
            if (device == null) {
                throw new BusinessException("设备不存在");
            }
        }

        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            if (contract.getEndDate().isBefore(contract.getStartDate())) {
                throw new BusinessException("结束日期不能早于开始日期");
            }
        }

        contract.setContractCode(generateContractCode());
        if (contract.getStatus() == null) {
            contract.setStatus(1);
        }
        maintenanceContractMapper.insert(contract);
        return contract;
    }

    @Transactional(rollbackFor = Exception.class)
    public MaintenanceContract updateContract(MaintenanceContract contract) {
        MaintenanceContract existing = maintenanceContractMapper.selectById(contract.getId());
        if (existing == null) {
            throw new BusinessException("维保合同不存在");
        }

        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            if (contract.getEndDate().isBefore(contract.getStartDate())) {
                throw new BusinessException("结束日期不能早于开始日期");
            }
        }

        maintenanceContractMapper.updateById(contract);
        return maintenanceContractMapper.selectById(contract.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteContract(Long id) {
        MaintenanceContract contract = maintenanceContractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("维保合同不存在");
        }
        maintenanceContractMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateContractStatus(Long id, Integer status) {
        MaintenanceContract contract = maintenanceContractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("维保合同不存在");
        }

        Integer currentStatus = contract.getStatus();
        validateStatusTransition(currentStatus, status);

        contract.setStatus(status);
        maintenanceContractMapper.updateById(contract);
    }

    public List<MaintenanceContract> getExpiringContracts(int days) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(days);

        LambdaQueryWrapper<MaintenanceContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaintenanceContract::getStatus, 2)
                .le(MaintenanceContract::getEndDate, expiryDate)
                .ge(MaintenanceContract::getEndDate, today)
                .orderByAsc(MaintenanceContract::getEndDate);

        return maintenanceContractMapper.selectList(wrapper);
    }

    private void validateStatusTransition(Integer currentStatus, Integer targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new BusinessException("状态不能为空");
        }

        switch (currentStatus) {
            case 1:
                if (targetStatus != 2 && targetStatus != 4) {
                    throw new BusinessException("待生效合同只能变更为已生效或已终止");
                }
                break;
            case 2:
                if (targetStatus != 3 && targetStatus != 4) {
                    throw new BusinessException("已生效合同只能变更为已到期或已终止");
                }
                break;
            case 3:
                if (targetStatus != 4) {
                    throw new BusinessException("已到期合同只能变更为已终止");
                }
                break;
            case 4:
                throw new BusinessException("已终止合同无法变更状态");
            default:
                throw new BusinessException("未知的合同状态");
        }
    }

    private String generateContractCode() {
        return "MC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" +
               String.format("%04d", System.currentTimeMillis() % 10000);
    }
}
