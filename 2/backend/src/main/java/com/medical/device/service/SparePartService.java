package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.SparePart;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.SparePartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SparePartService {

    private final SparePartMapper sparePartMapper;

    public PageResult<SparePart> listParts(int pageNum, int pageSize, String keyword,
                                           Integer minStock, Long manufacturer) {
        Page<SparePart> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SparePart> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SparePart::getPartName, keyword)
                    .or().like(SparePart::getPartCode, keyword)
                    .or().like(SparePart::getPartModel, keyword)
                    .or().like(SparePart::getManufacturer, keyword));
        }
        if (minStock != null) {
            wrapper.le(SparePart::getStockQuantity, minStock);
        }

        wrapper.orderByDesc(SparePart::getId);
        IPage<SparePart> result = sparePartMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public SparePart getPart(Long id) {
        SparePart part = sparePartMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("配件不存在");
        }
        return part;
    }

    @Transactional(rollbackFor = Exception.class)
    public SparePart createPart(SparePart part) {
        LambdaQueryWrapper<SparePart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SparePart::getPartCode, part.getPartCode());
        Long count = sparePartMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("配件编号已存在");
        }

        if (part.getStockQuantity() == null) {
            part.setStockQuantity(0);
        }
        if (part.getMinStock() == null) {
            part.setMinStock(10);
        }

        sparePartMapper.insert(part);
        return part;
    }

    @Transactional(rollbackFor = Exception.class)
    public SparePart updatePart(SparePart part) {
        SparePart existing = sparePartMapper.selectById(part.getId());
        if (existing == null) {
            throw new BusinessException("配件不存在");
        }

        if (part.getPartCode() != null && !part.getPartCode().equals(existing.getPartCode())) {
            LambdaQueryWrapper<SparePart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SparePart::getPartCode, part.getPartCode())
                    .ne(SparePart::getId, part.getId());
            Long count = sparePartMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("配件编号已存在");
            }
        }

        sparePartMapper.updateById(part);
        return sparePartMapper.selectById(part.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePart(Long id) {
        SparePart part = sparePartMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("配件不存在");
        }
        sparePartMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }

        SparePart part = sparePartMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("配件不存在");
        }

        int currentStock = part.getStockQuantity() != null ? part.getStockQuantity() : 0;
        part.setStockQuantity(currentStock + quantity);
        sparePartMapper.updateById(part);
    }

    @Transactional(rollbackFor = Exception.class)
    public void stockOut(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("出库数量必须大于0");
        }

        SparePart part = sparePartMapper.selectById(id);
        if (part == null) {
            throw new BusinessException("配件不存在");
        }

        int currentStock = part.getStockQuantity() != null ? part.getStockQuantity() : 0;
        if (currentStock < quantity) {
            throw new BusinessException("库存不足，当前库存：" + currentStock);
        }

        part.setStockQuantity(currentStock - quantity);
        sparePartMapper.updateById(part);
    }

    public List<SparePart> getLowStockParts() {
        LambdaQueryWrapper<SparePart> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.apply("stock_quantity <= min_stock")
                .or().isNull(SparePart::getStockQuantity))
                .orderByAsc(SparePart::getStockQuantity);

        return sparePartMapper.selectList(wrapper);
    }
}
