package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.common.BusinessException;
import com.bakery.dto.AnalysisVO;
import com.bakery.entity.ProdBatch;
import com.bakery.entity.StockLog;
import com.bakery.mapper.ProdBatchMapper;
import com.bakery.mapper.StockLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ProdBatchService extends ServiceImpl<ProdBatchMapper, ProdBatch> {

    @Autowired
    private StockLogMapper stockLogMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String WARNING_CACHE_KEY = "bakery:warning:stats";

    public IPage<ProdBatch> getBatchPage(Integer pageNum, Integer pageSize, Long recipeId,
                                         Long storeId, String batchNo, Integer warningType) {
        Page<ProdBatch> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectBatchPage(page, recipeId, storeId, batchNo, warningType);
    }

    public List<ProdBatch> getAvailableBatches(Long recipeId, Long storeId) {
        return baseMapper.selectAvailableBatch(recipeId, storeId);
    }

    public BigDecimal getRemainQty(Long batchId) {
        return baseMapper.getRemainQty(batchId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void outboundBatch(Long batchId, BigDecimal qty, String bizType, String bizNo, Long operatorStore) {
        BigDecimal remainQty = getRemainQty(batchId);
        if (remainQty.compareTo(qty) < 0) {
            throw new BusinessException("批次库存不足，剩余: " + remainQty);
        }

        ProdBatch batch = getById(batchId);
        if (batch.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("批次已过期，无法出库");
        }

        StockLog log = new StockLog();
        log.setStoreId(operatorStore);
        log.setBizType(bizType);
        log.setBizNo(bizNo);
        log.setBatchId(batchId);
        log.setRecipeId(batch.getRecipeId());
        log.setQtyBefore(remainQty);
        log.setQtyChange(qty.negate());
        log.setQtyAfter(remainQty.subtract(qty));
        log.setOperator("admin");
        log.setCreateTime(LocalDateTime.now());
        stockLogMapper.insert(log);

        BigDecimal newRemain = remainQty.subtract(qty);
        if (newRemain.compareTo(BigDecimal.ZERO) == 0) {
            baseMapper.updateStatus(batchId, 3);
        } else {
            baseMapper.updateStatus(batchId, 2);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void inboundBatch(Long batchId, BigDecimal qty, String bizType, String bizNo, Long operatorStore) {
        ProdBatch batch = getById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        StockLog log = new StockLog();
        log.setStoreId(operatorStore);
        log.setBizType(bizType);
        log.setBizNo(bizNo);
        log.setBatchId(batchId);
        log.setRecipeId(batch.getRecipeId());
        log.setQtyBefore(getRemainQty(batchId));
        log.setQtyChange(qty);
        log.setQtyAfter(getRemainQty(batchId).add(qty));
        log.setOperator("admin");
        log.setCreateTime(LocalDateTime.now());
        stockLogMapper.insert(log);
    }

    public AnalysisVO.WarningStatsVO getWarningStats(Long storeId) {
        String cacheKey = WARNING_CACHE_KEY + ":" + storeId;
        AnalysisVO.WarningStatsVO cached = (AnalysisVO.WarningStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        AnalysisVO.WarningStatsVO stats = new AnalysisVO.WarningStatsVO();
        stats.setExpiringCount(baseMapper.countExpiring(storeId));
        stats.setExpiredCount(baseMapper.countExpired(storeId));

        List<ProdBatch> allBatches = list(new LambdaQueryWrapper<ProdBatch>()
                .in(ProdBatch::getStatus, 1, 2)
                .eq(ProdBatch::getStoreId, storeId));

        int normalCount = 0;
        BigDecimal normalQty = BigDecimal.ZERO;
        BigDecimal expiringQty = BigDecimal.ZERO;
        BigDecimal expiredQty = BigDecimal.ZERO;

        LocalDateTime now = LocalDateTime.now();

        for (ProdBatch batch : allBatches) {
            BigDecimal remain = getRemainQty(batch.getId());
            if (batch.getExpireTime().isBefore(now)) {
                expiredQty = expiredQty.add(remain);
            } else {
                normalCount++;
                normalQty = normalQty.add(remain);
            }
        }

        List<ProdBatch> expiringBatches = baseMapper.selectWarningList(storeId);
        for (ProdBatch batch : expiringBatches) {
            expiringQty = expiringQty.add(getRemainQty(batch.getId()));
        }

        stats.setExpiringQty(expiringQty);
        stats.setExpiredQty(expiredQty);
        stats.setNormalCount(normalCount);
        stats.setNormalQty(normalQty);

        redisTemplate.opsForValue().set(cacheKey, stats, 5, TimeUnit.MINUTES);
        return stats;
    }

    public List<ProdBatch> getWarningList(Long storeId) {
        return baseMapper.selectWarningList(storeId);
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void clearWarningCache() {
        Set<String> keys = redisTemplate.keys(WARNING_CACHE_KEY + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public Map<String, Object> getBatchDetail(Long id) {
        ProdBatch batch = getById(id);
        BigDecimal remainQty = getRemainQty(id);
        Map<String, Object> result = new HashMap<>();
        result.put("batch", batch);
        result.put("remainQty", remainQty);
        return result;
    }
}
