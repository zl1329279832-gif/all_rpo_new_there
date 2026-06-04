package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.entity.BaseMaterial;
import com.bakery.entity.BaseMaterialStock;
import com.bakery.mapper.BaseMaterialMapper;
import com.bakery.mapper.BaseMaterialStockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService extends ServiceImpl<BaseMaterialMapper, BaseMaterial> {

    @Autowired
    private BaseMaterialStockMapper materialStockMapper;

    public List<BaseMaterial> list(String category, String keyword) {
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterial::getStatus, 1);
        if (category != null) {
            wrapper.eq(BaseMaterial::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(BaseMaterial::getMaterialName, keyword)
                    .or().like(BaseMaterial::getMaterialCode, keyword);
        }
        wrapper.orderByDesc(BaseMaterial::getCreateTime);
        return list(wrapper);
    }

    public List<BaseMaterialStock> getStockList(Long materialId, Long storeId) {
        return materialStockMapper.selectAvailableStock(materialId, storeId);
    }
}
