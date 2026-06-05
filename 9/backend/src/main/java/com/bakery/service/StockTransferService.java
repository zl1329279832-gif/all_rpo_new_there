package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.common.BusinessException;
import com.bakery.dto.StockTransferDTO;
import com.bakery.entity.BaseRecipe;
import com.bakery.entity.ProdBatch;
import com.bakery.entity.StockTransfer;
import com.bakery.entity.StockTransferDetail;
import com.bakery.mapper.BaseRecipeMapper;
import com.bakery.mapper.ProdBatchMapper;
import com.bakery.mapper.StockTransferDetailMapper;
import com.bakery.mapper.StockTransferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockTransferService extends ServiceImpl<StockTransferMapper, StockTransfer> {

    @Autowired
    private StockTransferDetailMapper transferDetailMapper;
    @Autowired
    private ProdBatchMapper batchMapper;
    @Autowired
    private ProdBatchService batchService;
    @Autowired
    private BaseRecipeMapper recipeMapper;

    public IPage<StockTransfer> getTransferPage(Integer pageNum, Integer pageSize, String transferNo, Integer transferType, Integer status) {
        Page<StockTransfer> page = new Page<>(pageNum, pageSize);
        IPage<StockTransfer> result = baseMapper.selectTransferPage(page, transferNo, transferType, status);
        for (StockTransfer transfer : result.getRecords()) {
            transfer.setTransferQty(transfer.getTotalQty());
            List<StockTransferDetail> details = transferDetailMapper.selectByTransferId(transfer.getId());
            if (details != null && !details.isEmpty()) {
                StockTransferDetail detail = details.get(0);
                transfer.setRecipeId(detail.getRecipeId());
                ProdBatch batch = batchMapper.selectById(detail.getBatchId());
                if (batch != null) {
                    transfer.setOutboundBatchNo(batch.getBatchNo());
                }
                List<ProdBatch> inBatches = batchMapper.selectList(new LambdaQueryWrapper<ProdBatch>()
                        .likeRight(ProdBatch::getRemark, "调拨入库，源批次:")
                        .eq(ProdBatch::getStoreId, transfer.getInStoreId()));
                if (inBatches != null && !inBatches.isEmpty()) {
                    transfer.setInboundBatchNo(inBatches.get(0).getBatchNo());
                }
                BaseRecipe recipe = recipeMapper.selectById(detail.getRecipeId());
                if (recipe != null) {
                    transfer.setProductName(recipe.getProductName());
                }
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(StockTransferDTO dto) {
        if (dto.getOutStoreId().equals(dto.getInStoreId())) {
            throw new BusinessException("调出门店和调入门店不能相同");
        }

        StockTransfer transfer = new StockTransfer();
        transfer.setTransferNo(generateTransferNo());
        transfer.setOutStoreId(dto.getOutStoreId());
        transfer.setInStoreId(dto.getInStoreId());
        transfer.setTransferType(dto.getTransferType());
        transfer.setStatus(0);
        transfer.setTotalQty(dto.getDetails().stream()
                .map(StockTransferDTO.TransferDetailDTO::getTransferQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        transfer.setRemark(dto.getRemark());
        save(transfer);

        for (StockTransferDTO.TransferDetailDTO detailDTO : dto.getDetails()) {
            ProdBatch batch = batchMapper.selectById(detailDTO.getBatchId());
            if (batch == null) {
                throw new BusinessException("批次不存在");
            }
            if (!batch.getStoreId().equals(dto.getOutStoreId())) {
                throw new BusinessException("批次不属于调出门店");
            }
            BigDecimal remainQty = batchService.getRemainQty(detailDTO.getBatchId());
            if (remainQty.compareTo(detailDTO.getTransferQty()) < 0) {
                throw new BusinessException("批次库存不足");
            }

            StockTransferDetail detail = new StockTransferDetail();
            detail.setTransferId(transfer.getId());
            detail.setBatchId(detailDTO.getBatchId());
            detail.setRecipeId(detailDTO.getRecipeId());
            detail.setTransferQty(detailDTO.getTransferQty());
            transferDetailMapper.insert(detail);
        }

        return transfer.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmOutbound(Long id) {
        StockTransfer transfer = getById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (transfer.getStatus() != 0) {
            throw new BusinessException("调拨单状态不正确，无法出库");
        }

        List<StockTransferDetail> details = transferDetailMapper.selectByTransferId(id);
        for (StockTransferDetail detail : details) {
            batchService.outboundBatch(detail.getBatchId(), detail.getTransferQty(),
                    "TRANSFER_OUT", transfer.getTransferNo(), transfer.getOutStoreId());
        }

        transfer.setStatus(1);
        transfer.setOutboundTime(LocalDateTime.now());
        updateById(transfer);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmInbound(Long id) {
        StockTransfer transfer = getById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (transfer.getStatus() != 1) {
            throw new BusinessException("调拨单状态不正确，无法入库");
        }

        List<StockTransferDetail> details = transferDetailMapper.selectByTransferId(id);
        for (StockTransferDetail detail : details) {
            ProdBatch origBatch = batchMapper.selectById(detail.getBatchId());

            ProdBatch newBatch = new ProdBatch();
            newBatch.setBatchNo(origBatch.getBatchNo() + "-T" + transfer.getId());
            newBatch.setRecipeId(detail.getRecipeId());
            newBatch.setPlanId(origBatch.getPlanId());
            newBatch.setStoreId(transfer.getInStoreId());
            newBatch.setProduceQty(detail.getTransferQty());
            newBatch.setProduceTime(origBatch.getProduceTime());
            newBatch.setExpireTime(origBatch.getExpireTime());
            newBatch.setStatus(1);
            newBatch.setRemark("调拨入库，源批次: " + origBatch.getBatchNo());
            batchMapper.insert(newBatch);

            batchService.inboundBatch(newBatch.getId(), detail.getTransferQty(),
                    "TRANSFER_IN", transfer.getTransferNo(), transfer.getInStoreId());
        }

        transfer.setStatus(2);
        transfer.setInboundTime(LocalDateTime.now());
        updateById(transfer);
    }

    public StockTransfer getTransferDetail(Long id) {
        StockTransfer transfer = getById(id);
        transfer.setTransferQty(transfer.getTotalQty());
        List<StockTransferDetail> details = transferDetailMapper.selectByTransferId(id);
        if (details != null && !details.isEmpty()) {
            StockTransferDetail detail = details.get(0);
            transfer.setRecipeId(detail.getRecipeId());
            ProdBatch batch = batchMapper.selectById(detail.getBatchId());
            if (batch != null) {
                transfer.setOutboundBatchNo(batch.getBatchNo());
            }
            List<ProdBatch> inBatches = batchMapper.selectList(new LambdaQueryWrapper<ProdBatch>()
                    .likeRight(ProdBatch::getRemark, "调拨入库，源批次:")
                    .eq(ProdBatch::getStoreId, transfer.getInStoreId()));
            if (inBatches != null && !inBatches.isEmpty()) {
                transfer.setInboundBatchNo(inBatches.get(0).getBatchNo());
            }
            BaseRecipe recipe = recipeMapper.selectById(detail.getRecipeId());
            if (recipe != null) {
                transfer.setProductName(recipe.getProductName());
            }
        }
        return transfer;
    }

    private String generateTransferNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = count(new LambdaQueryWrapper<StockTransfer>()
                .likeRight(StockTransfer::getTransferNo, "DB" + dateStr)) + 1;
        return "DB" + dateStr + String.format("%03d", count);
    }
}
