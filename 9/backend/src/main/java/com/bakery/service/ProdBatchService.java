package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.common.BusinessException;
import com.bakery.dto.AnalysisVO;
import com.bakery.entity.BaseRecipe;
import com.bakery.entity.ProdBatch;
import com.bakery.entity.StockLog;
import com.bakery.mapper.BaseRecipeMapper;
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
    @Autowired
    private BaseRecipeMapper recipeMapper;

    private static final String WARNING_CACHE_KEY = "bakery:warning:stats";

    public IPage<ProdBatch> getBatchPage(Integer pageNum, Integer pageSize, Long recipeId,
                                         Long storeId, String batchNo, Integer warningType) {
        Page<ProdBatch> page = new Page<>(pageNum, pageSize);
        IPage<ProdBatch> result = baseMapper.selectBatchPage(page, recipeId, storeId, batchNo, warningType);
        LocalDateTime now = LocalDateTime.now();
        for (ProdBatch batch : result.getRecords()) {
            BigDecimal remain = getRemainQty(batch.getId());
            batch.setTotalQty(batch.getProduceQty());
            batch.setRemainQty(remain);
            batch.setOutboundQty(batch.getProduceQty().subtract(remain));
            batch.setRemainTimeDesc(calculateRemainTime(batch.getExpireTime()));
            if (batch.getExpireTime().isBefore(now)) {
                batch.setWarningLevel("expired");
            } else if (batch.getWarningHours() != null && batch.getExpireTime().isBefore(now.plusHours(batch.getWarningHours()))) {
                if (batch.getExpireTime().isBefore(now.plusHours(4))) {
                    batch.setWarningLevel("severe");
                } else {
                    batch.setWarningLevel("normal");
                }
            } else {
                batch.setWarningLevel("normal");
            }
        }
        return result;
    }

    public List<ProdBatch> getAvailableBatches(Long recipeId, Long storeId) {
        List<ProdBatch> batches = baseMapper.selectAvailableBatch(recipeId, storeId);
        for (ProdBatch batch : batches) {
            BigDecimal remain = getRemainQty(batch.getId());
            batch.setTotalQty(batch.getProduceQty());
            batch.setRemainQty(remain);
            batch.setOutboundQty(batch.getProduceQty().subtract(remain));
        }
        return batches;
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
        stats.setWarningBatches(baseMapper.countExpiring(storeId));
        stats.setExpiredBatches(baseMapper.countExpired(storeId));

        List<ProdBatch> allBatches = list(new LambdaQueryWrapper<ProdBatch>()
                .in(ProdBatch::getStatus, 1, 2)
                .eq(ProdBatch::getStoreId, storeId));

        stats.setTotalBatches(allBatches.size());

        BigDecimal totalRemainQty = BigDecimal.ZERO;
        BigDecimal warningQty = BigDecimal.ZERO;
        BigDecimal expiredQty = BigDecimal.ZERO;
        Set<Long> warningProductIds = new java.util.HashSet<>();

        LocalDateTime now = LocalDateTime.now();

        for (ProdBatch batch : allBatches) {
            BigDecimal remain = getRemainQty(batch.getId());
            totalRemainQty = totalRemainQty.add(remain);
            batch.setTotalQty(batch.getProduceQty());
            batch.setRemainQty(remain);
            batch.setOutboundQty(batch.getProduceQty().subtract(remain));
            if (batch.getExpireTime().isBefore(now)) {
                expiredQty = expiredQty.add(remain);
            }
        }

        List<ProdBatch> expiringBatches = baseMapper.selectWarningList(storeId);
        for (ProdBatch batch : expiringBatches) {
            BigDecimal remain = getRemainQty(batch.getId());
            warningQty = warningQty.add(remain);
            warningProductIds.add(batch.getRecipeId());
            batch.setTotalQty(batch.getProduceQty());
            batch.setRemainQty(remain);
            batch.setOutboundQty(batch.getProduceQty().subtract(remain));
            batch.setRemainTimeDesc(calculateRemainTime(batch.getExpireTime()));
            if (batch.getExpireTime().isBefore(now.plusHours(4))) {
                batch.setWarningLevel("severe");
            } else {
                batch.setWarningLevel("normal");
            }
        }

        stats.setTotalRemainQty(totalRemainQty);
        stats.setWarningQty(warningQty);
        stats.setExpiredQty(expiredQty);
        stats.setWarningProducts(warningProductIds.size());

        redisTemplate.opsForValue().set(cacheKey, stats, 5, TimeUnit.MINUTES);
        return stats;
    }

    private String calculateRemainTime(LocalDateTime expireTime) {
        LocalDateTime now = LocalDateTime.now();
        if (expireTime.isBefore(now)) {
            return "已过期";
        }
        long hours = java.time.Duration.between(now, expireTime).toHours();
        if (hours < 24) {
            return hours + "小时后过期";
        }
        long days = hours / 24;
        long remainHours = hours % 24;
        if (remainHours == 0) {
            return days + "天后过期";
        }
        return days + "天" + remainHours + "小时后过期";
    }

    public List<ProdBatch> getWarningList(Long storeId) {
        List<ProdBatch> batches = baseMapper.selectWarningList(storeId);
        LocalDateTime now = LocalDateTime.now();
        for (ProdBatch batch : batches) {
            BigDecimal remain = getRemainQty(batch.getId());
            batch.setTotalQty(batch.getProduceQty());
            batch.setRemainQty(remain);
            batch.setOutboundQty(batch.getProduceQty().subtract(remain));
            batch.setRemainTimeDesc(calculateRemainTime(batch.getExpireTime()));
            if (batch.getExpireTime().isBefore(now.plusHours(4))) {
                batch.setWarningLevel("severe");
            } else {
                batch.setWarningLevel("normal");
            }
        }
        return batches;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void clearWarningCache() {
        Set<String> keys = redisTemplate.keys(WARNING_CACHE_KEY + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public ProdBatch getBatchDetail(Long id) {
        ProdBatch batch = getById(id);
        BigDecimal remainQty = getRemainQty(id);
        batch.setTotalQty(batch.getProduceQty());
        batch.setRemainQty(remainQty);
        batch.setOutboundQty(batch.getProduceQty().subtract(remainQty));
        batch.setDamageQty(BigDecimal.ZERO);
        return batch;
    }

    public IPage<ProdBatch> getWarningPage(Integer pageNum, Integer pageSize, Long storeId, String productName, String warningLevel) {
        Page<ProdBatch> page = new Page<>(pageNum, pageSize);
        Integer warningType = null;
        if ("expired".equals(warningLevel)) {
            warningType = 1;
        } else if ("severe".equals(warningLevel) || "normal".equals(warningLevel)) {
            warningType = 2;
        }
        IPage<ProdBatch> result = baseMapper.selectBatchPage(page, null, storeId, null, warningType);
        LocalDateTime now = LocalDateTime.now();
        java.util.List<ProdBatch> filtered = new java.util.ArrayList<>();
        for (ProdBatch batch : result.getRecords()) {
            if (productName != null && !productName.isEmpty() && batch.getProductName() != null
                    && !batch.getProductName().contains(productName)) {
                continue;
            }
            BigDecimal remain = getRemainQty(batch.getId());
            batch.setTotalQty(batch.getProduceQty());
            batch.setRemainQty(remain);
            batch.setOutboundQty(batch.getProduceQty().subtract(remain));
            batch.setRemainTimeDesc(calculateRemainTime(batch.getExpireTime()));
            String level;
            if (batch.getExpireTime().isBefore(now)) {
                level = "expired";
            } else if (batch.getExpireTime().isBefore(now.plusHours(4))) {
                level = "severe";
            } else {
                level = "normal";
            }
            batch.setWarningLevel(level);
            if (warningLevel == null || warningLevel.isEmpty() || warningLevel.equals(level)) {
                filtered.add(batch);
            }
        }
        result.setRecords(filtered);
        result.setTotal(filtered.size());
        return result;
    }

    public List<AnalysisVO.WarningByProductVO> getWarningByProduct(Long storeId) {
        List<ProdBatch> allBatches = list(new LambdaQueryWrapper<ProdBatch>()
                .in(ProdBatch::getStatus, 1, 2)
                .eq(ProdBatch::getStoreId, storeId));
        Map<String, AnalysisVO.WarningByProductVO> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (ProdBatch batch : allBatches) {
            try {
                BaseRecipe r = recipeMapper.selectById(batch.getRecipeId());
                if (r == null) continue;
                String productName = r.getProductName();
                BigDecimal remain = getRemainQty(batch.getId());
                AnalysisVO.WarningByProductVO vo = map.get(productName);
                if (vo == null) {
                    vo = new AnalysisVO.WarningByProductVO();
                    vo.setProductName(productName);
                    vo.setWarningQty(BigDecimal.ZERO);
                    vo.setExpiredQty(BigDecimal.ZERO);
                    map.put(productName, vo);
                }
                if (batch.getExpireTime().isBefore(now)) {
                    vo.setExpiredQty(vo.getExpiredQty().add(remain));
                } else if (batch.getWarningHours() != null && batch.getExpireTime().isBefore(now.plusHours(batch.getWarningHours()))) {
                    vo.setWarningQty(vo.getWarningQty().add(remain));
                }
            } catch (Exception e) {
                // skip
            }
        }
        return new ArrayList<>(map.values());
    }

    public IPage<Map<String, Object>> getProductOverview(Integer pageNum, Integer pageSize, Long storeId, String productName) {
        List<ProdBatch> allBatches = list(new LambdaQueryWrapper<ProdBatch>()
                .in(ProdBatch::getStatus, 1, 2)
                .eq(ProdBatch::getStoreId, storeId));
        Map<String, Map<String, Object>> productMap = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (ProdBatch batch : allBatches) {
            try {
                BaseRecipe r = recipeMapper.selectById(batch.getRecipeId());
                if (r == null) continue;
                String pName = r.getProductName();
                if (productName != null && !productName.isEmpty() && !pName.contains(productName)) {
                    continue;
                }
                BigDecimal remain = getRemainQty(batch.getId());
                String key = batch.getStoreId() + "_" + batch.getRecipeId();
                Map<String, Object> product = productMap.get(key);
                if (product == null) {
                    product = new HashMap<>();
                    product.put("productName", pName);
                    product.put("storeId", batch.getStoreId());
                    product.put("recipeId", batch.getRecipeId());
                    product.put("storeName", batch.getStoreName());
                    product.put("totalQty", BigDecimal.ZERO);
                    product.put("normalQty", BigDecimal.ZERO);
                    product.put("warningQty", BigDecimal.ZERO);
                    product.put("expiredQty", BigDecimal.ZERO);
                    product.put("earliestExpireTime", null);
                    productMap.put(key, product);
                }
                product.put("totalQty", ((BigDecimal) product.get("totalQty")).add(remain));
                if (batch.getExpireTime().isBefore(now)) {
                    product.put("expiredQty", ((BigDecimal) product.get("expiredQty")).add(remain));
                } else if (batch.getWarningHours() != null && batch.getExpireTime().isBefore(now.plusHours(batch.getWarningHours()))) {
                    product.put("warningQty", ((BigDecimal) product.get("warningQty")).add(remain));
                } else {
                    product.put("normalQty", ((BigDecimal) product.get("normalQty")).add(remain));
                }
                LocalDateTime currentEarliest = (LocalDateTime) product.get("earliestExpireTime");
                if (currentEarliest == null || batch.getExpireTime().isBefore(currentEarliest)) {
                    product.put("earliestExpireTime", batch.getExpireTime());
                }
            } catch (Exception e) {
                // skip
            }
        }
        List<Map<String, Object>> list = new ArrayList<>(productMap.values());
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageList = start < total ? list.subList(start, end) : new ArrayList<>();
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        page.setRecords(pageList);
        page.setTotal(total);
        return page;
    }
}
