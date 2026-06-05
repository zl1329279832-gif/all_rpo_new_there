package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.common.BusinessException;
import com.bakery.dto.StockDamageDTO;
import com.bakery.entity.BaseRecipe;
import com.bakery.entity.ProdBatch;
import com.bakery.entity.StockDamage;
import com.bakery.entity.StockDamageDetail;
import com.bakery.mapper.BaseRecipeMapper;
import com.bakery.mapper.ProdBatchMapper;
import com.bakery.mapper.StockDamageDetailMapper;
import com.bakery.mapper.StockDamageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StockDamageService extends ServiceImpl<StockDamageMapper, StockDamage> {

    @Autowired
    private StockDamageDetailMapper damageDetailMapper;
    @Autowired
    private ProdBatchService batchService;
    @Autowired
    private ProdBatchMapper batchMapper;
    @Autowired
    private BaseRecipeMapper recipeMapper;

    public IPage<StockDamage> getDamagePage(Integer pageNum, Integer pageSize, String damageNo, Integer damageType, Integer status) {
        Page<StockDamage> page = new Page<>(pageNum, pageSize);
        IPage<StockDamage> result = baseMapper.selectDamagePage(page, damageNo, damageType, status);
        for (StockDamage damage : result.getRecords()) {
            damage.setDamageQty(damage.getTotalQty());
            damage.setDamageAmount(damage.getTotalAmount());
            List<StockDamageDetail> details = damageDetailMapper.selectByDamageId(damage.getId());
            if (details != null && !details.isEmpty()) {
                StockDamageDetail detail = details.get(0);
                damage.setBatchId(detail.getBatchId());
                ProdBatch batch = batchMapper.selectById(detail.getBatchId());
                if (batch != null) {
                    damage.setBatchNo(batch.getBatchNo());
                }
                BaseRecipe recipe = recipeMapper.selectById(detail.getRecipeId());
                if (recipe != null) {
                    damage.setProductName(recipe.getProductName());
                }
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createDamage(StockDamageDTO dto) {
        StockDamage damage = new StockDamage();
        damage.setDamageNo(generateDamageNo());
        damage.setStoreId(dto.getStoreId());
        damage.setDamageType(dto.getDamageType());
        damage.setStatus(0);
        damage.setTotalQty(dto.getDetails().stream()
                .map(StockDamageDTO.DamageDetailDTO::getDamageQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        damage.setTotalAmount(dto.getDetails().stream()
                .map(d -> d.getUnitPrice().multiply(d.getDamageQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        damage.setReason(dto.getReason());
        damage.setCreateBy(1L);
        save(damage);

        for (StockDamageDTO.DamageDetailDTO detailDTO : dto.getDetails()) {
            BigDecimal remainQty = batchService.getRemainQty(detailDTO.getBatchId());
            if (remainQty.compareTo(detailDTO.getDamageQty()) < 0) {
                throw new BusinessException("批次库存不足，无法报损");
            }

            StockDamageDetail detail = new StockDamageDetail();
            detail.setDamageId(damage.getId());
            detail.setBatchId(detailDTO.getBatchId());
            detail.setRecipeId(detailDTO.getRecipeId());
            detail.setDamageQty(detailDTO.getDamageQty());
            detail.setUnitPrice(detailDTO.getUnitPrice());
            detail.setSubtotal(detailDTO.getUnitPrice().multiply(detailDTO.getDamageQty()));
            damageDetailMapper.insert(detail);
        }

        return damage.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditDamage(Long id, Integer status, String auditOpinion) {
        StockDamage damage = getById(id);
        if (damage == null) {
            throw new BusinessException("报损单不存在");
        }
        if (damage.getStatus() != 0) {
            throw new BusinessException("报损单状态不正确，无法审核");
        }

        if (status == 1) {
            List<StockDamageDetail> details = damageDetailMapper.selectByDamageId(id);
            for (StockDamageDetail detail : details) {
                batchService.outboundBatch(detail.getBatchId(), detail.getDamageQty(),
                        "DAMAGE", damage.getDamageNo(), damage.getStoreId());
            }
            damage.setStatus(1);
        } else if (status == 2) {
            damage.setStatus(2);
        } else {
            throw new BusinessException("审核状态不正确");
        }

        damage.setAuditBy(1L);
        damage.setAuditTime(LocalDateTime.now());
        updateById(damage);
    }

    public StockDamage getDamageDetail(Long id) {
        StockDamage damage = getById(id);
        damage.setDamageQty(damage.getTotalQty());
        damage.setDamageAmount(damage.getTotalAmount());
        List<StockDamageDetail> details = damageDetailMapper.selectByDamageId(id);
        if (details != null && !details.isEmpty()) {
            StockDamageDetail detail = details.get(0);
            damage.setBatchId(detail.getBatchId());
            ProdBatch batch = batchMapper.selectById(detail.getBatchId());
            if (batch != null) {
                damage.setBatchNo(batch.getBatchNo());
            }
            BaseRecipe recipe = recipeMapper.selectById(detail.getRecipeId());
            if (recipe != null) {
                damage.setProductName(recipe.getProductName());
            }
        }
        return damage;
    }

    private String generateDamageNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = count(new LambdaQueryWrapper<StockDamage>()
                .likeRight(StockDamage::getDamageNo, "BS" + dateStr)) + 1;
        return "BS" + dateStr + String.format("%03d", count);
    }
}
