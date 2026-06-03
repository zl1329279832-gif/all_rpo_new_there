package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.InventoryLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InventoryLogMapper {

    int insert(InventoryLog record);

    InventoryLog selectById(Long id);

    List<InventoryLog> selectList(@Param("query") PageQuery query,
                                   @Param("warehouseId") Long warehouseId,
                                   @Param("productId") Long productId,
                                   @Param("batchNo") String batchNo,
                                   @Param("businessType") Integer businessType,
                                   @Param("businessNo") String businessNo);

    List<InventoryLog> selectByBusinessNo(@Param("businessNo") String businessNo);
}
