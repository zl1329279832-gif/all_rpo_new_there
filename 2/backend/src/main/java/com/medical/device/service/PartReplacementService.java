package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.PartReplacement;
import com.medical.device.entity.RepairOrder;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.PartReplacementMapper;
import com.medical.device.mapper.RepairOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartReplacementService {

    private final PartReplacementMapper partReplacementMapper;
    private final RepairOrderMapper repairOrderMapper;

    public PageResult<PartReplacement> listPartReplacements(int pageNum, int pageSize,
                                                            String keyword, Long repairOrderId,
                                                            Long sparePartId, String operator) {
        Page<PartReplacement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PartReplacement> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(PartReplacement::getPartName, keyword)
                    .or().like(PartReplacement::getPartModel, keyword));
        }
        if (repairOrderId != null) {
            wrapper.eq(PartReplacement::getRepairOrderId, repairOrderId);
        }
        if (sparePartId != null) {
            wrapper.eq(PartReplacement::getSparePartId, sparePartId);
        }
        if (operator != null && !operator.isEmpty()) {
            wrapper.like(PartReplacement::getOperator, operator);
        }

        wrapper.orderByDesc(PartReplacement::getId);
        IPage<PartReplacement> result = partReplacementMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public PartReplacement getPartReplacement(Long id) {
        PartReplacement partReplacement = partReplacementMapper.selectById(id);
        if (partReplacement == null) {
            throw new BusinessException("配件更换记录不存在");
        }
        return partReplacement;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createPartReplacement(PartReplacement partReplacement) {
        RepairOrder repairOrder = repairOrderMapper.selectById(partReplacement.getRepairOrderId());
        if (repairOrder == null) {
            throw new BusinessException("关联的维修工单不存在");
        }

        if (partReplacement.getReplaceTime() == null) {
            partReplacement.setReplaceTime(LocalDateTime.now());
        }

        calculateTotalPrice(partReplacement);

        partReplacementMapper.insert(partReplacement);

        updateRepairOrderCost(partReplacement.getRepairOrderId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePartReplacement(PartReplacement partReplacement) {
        PartReplacement existing = partReplacementMapper.selectById(partReplacement.getId());
        if (existing == null) {
            throw new BusinessException("配件更换记录不存在");
        }

        if (partReplacement.getRepairOrderId() != null
                && !partReplacement.getRepairOrderId().equals(existing.getRepairOrderId())) {
            RepairOrder repairOrder = repairOrderMapper.selectById(partReplacement.getRepairOrderId());
            if (repairOrder == null) {
                throw new BusinessException("关联的维修工单不存在");
            }
        }

        calculateTotalPrice(partReplacement);

        partReplacementMapper.updateById(partReplacement);

        Long orderId = partReplacement.getRepairOrderId() != null
                ? partReplacement.getRepairOrderId() : existing.getRepairOrderId();
        updateRepairOrderCost(orderId);

        if (existing.getRepairOrderId() != null
                && !existing.getRepairOrderId().equals(orderId)) {
            updateRepairOrderCost(existing.getRepairOrderId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePartReplacement(Long id) {
        PartReplacement partReplacement = partReplacementMapper.selectById(id);
        if (partReplacement == null) {
            throw new BusinessException("配件更换记录不存在");
        }

        Long repairOrderId = partReplacement.getRepairOrderId();
        partReplacementMapper.deleteById(id);

        if (repairOrderId != null) {
            updateRepairOrderCost(repairOrderId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePartReplacements(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的记录");
        }

        List<PartReplacement> replacements = partReplacementMapper.selectBatchIds(ids);
        if (replacements.isEmpty()) {
            throw new BusinessException("未找到要删除的记录");
        }

        List<Long> orderIds = replacements.stream()
                .map(PartReplacement::getRepairOrderId)
                .distinct()
                .toList();

        partReplacementMapper.deleteBatchIds(ids);

        for (Long orderId : orderIds) {
            if (orderId != null) {
                updateRepairOrderCost(orderId);
            }
        }
    }

    public List<PartReplacement> getPartReplacementsByRepairOrderId(Long repairOrderId) {
        LambdaQueryWrapper<PartReplacement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PartReplacement::getRepairOrderId, repairOrderId);
        wrapper.orderByDesc(PartReplacement::getId);
        return partReplacementMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchCreatePartReplacements(Long repairOrderId, List<PartReplacement> replacements) {
        RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
        if (repairOrder == null) {
            throw new BusinessException("关联的维修工单不存在");
        }

        for (PartReplacement replacement : replacements) {
            replacement.setRepairOrderId(repairOrderId);
            if (replacement.getReplaceTime() == null) {
                replacement.setReplaceTime(LocalDateTime.now());
            }
            calculateTotalPrice(replacement);
            partReplacementMapper.insert(replacement);
        }

        updateRepairOrderCost(repairOrderId);
    }

    public BigDecimal calculateTotalCostByRepairOrderId(Long repairOrderId) {
        List<PartReplacement> replacements = getPartReplacementsByRepairOrderId(repairOrderId);
        return replacements.stream()
                .map(r -> r.getTotalPrice() != null ? r.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculateTotalPrice(PartReplacement partReplacement) {
        if (partReplacement.getQuantity() != null && partReplacement.getUnitPrice() != null) {
            BigDecimal totalPrice = partReplacement.getUnitPrice()
                    .multiply(BigDecimal.valueOf(partReplacement.getQuantity()));
            partReplacement.setTotalPrice(totalPrice);
        }
    }

    private void updateRepairOrderCost(Long repairOrderId) {
        if (repairOrderId == null) {
            return;
        }
        BigDecimal totalPartCost = calculateTotalCostByRepairOrderId(repairOrderId);
        RepairOrder repairOrder = repairOrderMapper.selectById(repairOrderId);
        if (repairOrder != null) {
            repairOrder.setRepairCost(totalPartCost);
            repairOrderMapper.updateById(repairOrder);
        }
    }
}
