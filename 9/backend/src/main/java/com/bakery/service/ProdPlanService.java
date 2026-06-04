package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.common.BusinessException;
import com.bakery.dto.ProdPlanDTO;
import com.bakery.dto.ProduceCompleteDTO;
import com.bakery.entity.*;
import com.bakery.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProdPlanService extends ServiceImpl<ProdPlanMapper, ProdPlan> {

    @Autowired
    private ProdPlanDetailMapper planDetailMapper;
    @Autowired
    private BaseMaterialStockMapper materialStockMapper;
    @Autowired
    private BaseRecipeDetailMapper recipeDetailMapper;
    @Autowired
    private BaseRecipeMapper recipeMapper;
    @Autowired
    private ProdBatchMapper batchMapper;
    @Autowired
    private StockLogMapper stockLogMapper;

    public IPage<ProdPlan> getPlanPage(Integer pageNum, Integer pageSize, LocalDate planDate, Integer status) {
        Page<ProdPlan> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPlanPage(page, planDate, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(ProdPlanDTO dto) {
        validateMaterialStock(dto, 1L);

        ProdPlan plan = new ProdPlan();
        plan.setPlanNo(generatePlanNo());
        plan.setPlanDate(dto.getPlanDate());
        plan.setStoreId(1L);
        plan.setStatus(0);
        plan.setRemark(dto.getRemark());
        plan.setCreateBy(1L);
        save(plan);

        for (ProdPlanDTO.PlanDetailDTO detailDTO : dto.getDetails()) {
            ProdPlanDetail detail = new ProdPlanDetail();
            detail.setPlanId(plan.getId());
            detail.setRecipeId(detailDTO.getRecipeId());
            detail.setPlanQty(detailDTO.getPlanQty());
            detail.setActualQty(BigDecimal.ZERO);
            planDetailMapper.insert(detail);
        }

        return plan.getId();
    }

    private void validateMaterialStock(ProdPlanDTO dto, Long storeId) {
        List<Long> recipeIds = dto.getDetails().stream()
                .map(ProdPlanDTO.PlanDetailDTO::getRecipeId)
                .collect(Collectors.toList());

        List<BaseRecipeDetail> allRecipeDetails = recipeDetailMapper.selectList(
                new LambdaQueryWrapper<BaseRecipeDetail>()
                        .in(BaseRecipeDetail::getRecipeId, recipeIds)
        );

        Map<Long, List<BaseRecipeDetail>> recipeDetailMap = allRecipeDetails.stream()
                .collect(Collectors.groupingBy(BaseRecipeDetail::getRecipeId));

        Map<Long, BigDecimal> materialRequiredMap = new HashMap<>();

        for (ProdPlanDTO.PlanDetailDTO detailDTO : dto.getDetails()) {
            List<BaseRecipeDetail> recipeDetails = recipeDetailMap.get(detailDTO.getRecipeId());
            if (recipeDetails == null || recipeDetails.isEmpty()) {
                throw new BusinessException("配方ID " + detailDTO.getRecipeId() + " 不存在");
            }

            BigDecimal scale = detailDTO.getPlanQty().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            for (BaseRecipeDetail recipeDetail : recipeDetails) {
                Long materialId = recipeDetail.getMaterialId();
                BigDecimal required = recipeDetail.getDosage().multiply(scale);
                materialRequiredMap.merge(materialId, required, BigDecimal::add);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, BigDecimal> entry : materialRequiredMap.entrySet()) {
            BigDecimal available = materialStockMapper.getAvailableStock(entry.getKey(), storeId);
            if (available.compareTo(entry.getValue()) < 0) {
                sb.append("原料ID ").append(entry.getKey())
                        .append(" 库存不足，需要 ").append(entry.getValue())
                        .append("，可用 ").append(available).append("; ");
            }
        }

        if (sb.length() > 0) {
            throw new BusinessException(sb.toString());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditPlan(Long id) {
        ProdPlan plan = getById(id);
        if (plan == null) {
            throw new BusinessException("生产计划不存在");
        }
        if (plan.getStatus() != 0) {
            throw new BusinessException("计划状态不正确，无法审核");
        }
        plan.setStatus(1);
        updateById(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void startProduce(Long id) {
        ProdPlan plan = getById(id);
        if (plan == null) {
            throw new BusinessException("生产计划不存在");
        }
        if (plan.getStatus() != 1) {
            throw new BusinessException("计划状态不正确，无法开始生产");
        }
        plan.setStatus(2);
        updateById(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeProduce(ProduceCompleteDTO dto) {
        ProdPlan plan = getById(dto.getPlanId());
        if (plan == null) {
            throw new BusinessException("生产计划不存在");
        }
        if (plan.getStatus() != 2) {
            throw new BusinessException("计划状态不正确，无法完成生产");
        }

        List<Long> recipeIds = dto.getItems().stream()
                .map(ProduceCompleteDTO.ProduceItem::getRecipeId)
                .collect(Collectors.toList());

        List<BaseRecipeDetail> allRecipeDetails = recipeDetailMapper.selectList(
                new LambdaQueryWrapper<BaseRecipeDetail>()
                        .in(BaseRecipeDetail::getRecipeId, recipeIds)
        );
        Map<Long, List<BaseRecipeDetail>> recipeDetailMap = allRecipeDetails.stream()
                .collect(Collectors.groupingBy(BaseRecipeDetail::getRecipeId));

        for (ProduceCompleteDTO.ProduceItem item : dto.getItems()) {
            ProdPlanDetail detail = planDetailMapper.selectById(item.getPlanDetailId());
            if (detail == null) {
                throw new BusinessException("计划明细不存在");
            }
            detail.setActualQty(item.getActualQty());
            planDetailMapper.updateById(detail);

            List<BaseRecipeDetail> recipeDetails = recipeDetailMap.get(item.getRecipeId());
            if (recipeDetails != null) {
                BigDecimal scale = item.getActualQty().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                for (BaseRecipeDetail recipeDetail : recipeDetails) {
                    deductMaterialStock(recipeDetail.getMaterialId(), 1L,
                            recipeDetail.getDosage().multiply(scale));
                }
            }

            BaseRecipe recipe = recipeMapper.selectById(item.getRecipeId());

            ProdBatch batch = new ProdBatch();
            batch.setBatchNo(generateBatchNo());
            batch.setRecipeId(item.getRecipeId());
            batch.setPlanId(dto.getPlanId());
            batch.setStoreId(1L);
            batch.setProduceQty(item.getActualQty());
            batch.setProduceTime(item.getProduceTime() != null ? item.getProduceTime() : LocalDateTime.now());
            batch.setExpireTime(batch.getProduceTime().plusHours(recipe.getShelfLifeHours()));
            batch.setStatus(1);
            batch.setRemark("生产计划: " + plan.getPlanNo());
            batchMapper.insert(batch);

            StockLog log = new StockLog();
            log.setStoreId(1L);
            log.setBizType("PRODUCE");
            log.setBizNo(batch.getBatchNo());
            log.setBatchId(batch.getId());
            log.setRecipeId(item.getRecipeId());
            log.setQtyBefore(BigDecimal.ZERO);
            log.setQtyChange(item.getActualQty());
            log.setQtyAfter(item.getActualQty());
            log.setOperator("admin");
            log.setCreateTime(LocalDateTime.now());
            stockLogMapper.insert(log);
        }

        plan.setStatus(3);
        updateById(plan);
    }

    private void deductMaterialStock(Long materialId, Long storeId, BigDecimal qty) {
        List<BaseMaterialStock> stocks = materialStockMapper.selectAvailableStock(materialId, storeId);
        BigDecimal remaining = qty;

        for (BaseMaterialStock stock : stocks) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal deductQty = stock.getQuantity().min(remaining);
            materialStockMapper.deductStock(stock.getId(), deductQty);
            remaining = remaining.subtract(deductQty);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("原料扣减失败，库存不足");
        }
    }

    public Map<String, Object> getPlanDetail(Long id) {
        ProdPlan plan = getById(id);
        List<ProdPlanDetail> details = planDetailMapper.selectByPlanId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("details", details);
        return result;
    }

    private String generatePlanNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = count(new LambdaQueryWrapper<ProdPlan>()
                .likeRight(ProdPlan::getPlanNo, "PL" + dateStr)) + 1;
        return "PL" + dateStr + String.format("%03d", count);
    }

    private String generateBatchNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = batchMapper.selectCount(new LambdaQueryWrapper<ProdBatch>()
                .likeRight(ProdBatch::getBatchNo, "PC" + dateStr)) + 1;
        return "PC" + dateStr + String.format("%03d", count);
    }
}
