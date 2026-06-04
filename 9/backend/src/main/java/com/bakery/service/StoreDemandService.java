package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.common.BusinessException;
import com.bakery.entity.StoreDemand;
import com.bakery.entity.StoreDemandDetail;
import com.bakery.mapper.StoreDemandDetailMapper;
import com.bakery.mapper.StoreDemandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreDemandService extends ServiceImpl<StoreDemandMapper, StoreDemand> {

    @Autowired
    private StoreDemandDetailMapper demandDetailMapper;

    public IPage<StoreDemand> getDemandPage(Integer pageNum, Integer pageSize, Integer status) {
        Page<StoreDemand> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectDemandPage(page, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmDemand(Long id, List<StoreDemandDetail> details) {
        StoreDemand demand = getById(id);
        if (demand == null) {
            throw new BusinessException("需求单不存在");
        }
        if (demand.getStatus() != 0) {
            throw new BusinessException("需求单状态不正确，无法确认");
        }

        for (StoreDemandDetail detail : details) {
            StoreDemandDetail existDetail = demandDetailMapper.selectById(detail.getId());
            if (existDetail == null || !existDetail.getDemandId().equals(id)) {
                throw new BusinessException("需求明细不存在");
            }
            if (detail.getConfirmQty().compareTo(detail.getDemandQty()) > 0) {
                throw new BusinessException("确认数量不能大于需求数量");
            }
            existDetail.setConfirmQty(detail.getConfirmQty());
            demandDetailMapper.updateById(existDetail);
        }

        demand.setStatus(1);
        updateById(demand);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deliverDemand(Long id, List<StoreDemandDetail> details) {
        StoreDemand demand = getById(id);
        if (demand == null) {
            throw new BusinessException("需求单不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException("需求单状态不正确，无法发货");
        }

        for (StoreDemandDetail detail : details) {
            StoreDemandDetail existDetail = demandDetailMapper.selectById(detail.getId());
            if (existDetail == null || !existDetail.getDemandId().equals(id)) {
                throw new BusinessException("需求明细不存在");
            }
            if (detail.getDeliverQty().compareTo(existDetail.getConfirmQty()) > 0) {
                throw new BusinessException("发货数量不能大于确认数量");
            }
            existDetail.setDeliverQty(detail.getDeliverQty());
            demandDetailMapper.updateById(existDetail);
        }

        demand.setStatus(2);
        updateById(demand);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeDemand(Long id) {
        StoreDemand demand = getById(id);
        if (demand == null) {
            throw new BusinessException("需求单不存在");
        }
        if (demand.getStatus() != 2) {
            throw new BusinessException("需求单状态不正确，无法完成");
        }
        demand.setStatus(3);
        updateById(demand);
    }

    public Map<String, Object> getDemandDetail(Long id) {
        StoreDemand demand = getById(id);
        List<StoreDemandDetail> details = demandDetailMapper.selectByDemandId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("demand", demand);
        result.put("details", details);
        return result;
    }

    public String generateDemandNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = count(new LambdaQueryWrapper<StoreDemand>()
                .likeRight(StoreDemand::getDemandNo, "XQ" + dateStr)) + 1;
        return "XQ" + dateStr + String.format("%03d", count);
    }
}
