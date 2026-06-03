package com.wms.mapper;

import com.wms.dto.InventoryQueryDTO;
import com.wms.entity.InventoryBatch;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryBatchMapper {

    int insert(InventoryBatch record);

    int updateById(InventoryBatch record);

    InventoryBatch selectById(Long id);

    List<InventoryBatch> selectList(InventoryQueryDTO query);

    List<InventoryBatch> selectByProductIdForOutbound(@Param("warehouseId") Long warehouseId,
                                                     @Param("productId") Long productId,
                                                     @Param("outboundStrategy") Integer outboundStrategy,
                                                     @Param("specifyBatchNo") String specifyBatchNo);

    @Select("SELECT * FROM wms_inventory_batch WHERE warehouse_id = #{warehouseId} AND product_id = #{productId} " +
            "AND batch_no = #{batchNo} AND location_id = #{locationId} FOR UPDATE")
    InventoryBatch selectForUpdate(@Param("warehouseId") Long warehouseId,
                                   @Param("productId") Long productId,
                                   @Param("batchNo") String batchNo,
                                   @Param("locationId") Long locationId);

    @Update("UPDATE wms_inventory_batch SET quantity = quantity + #{quantity}, " +
            "available_quantity = available_quantity + #{quantity}, " +
            "update_time = NOW() WHERE id = #{id}")
    int addQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET quantity = quantity - #{quantity}, " +
            "available_quantity = available_quantity - #{quantity}, " +
            "update_time = NOW() WHERE id = #{id} AND available_quantity >= #{quantity}")
    int reduceQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET available_quantity = available_quantity - #{quantity}, " +
            "locked_quantity = locked_quantity + #{quantity}, " +
            "update_time = NOW() WHERE id = #{id} AND available_quantity >= #{quantity}")
    int lockQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET available_quantity = available_quantity + #{quantity}, " +
            "locked_quantity = locked_quantity - #{quantity}, " +
            "update_time = NOW() WHERE id = #{id} AND locked_quantity >= #{quantity}")
    int unlockQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET locked_quantity = locked_quantity - #{quantity}, " +
            "quantity = quantity - #{quantity}, " +
            "update_time = NOW() WHERE id = #{id} AND locked_quantity >= #{quantity}")
    int reduceLockedQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET available_quantity = available_quantity - #{quantity}, " +
            "frozen_quantity = frozen_quantity + #{quantity}, " +
            "inventory_status = 4, " +
            "update_time = NOW() WHERE id = #{id} AND available_quantity >= #{quantity}")
    int freezeQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET available_quantity = available_quantity + #{quantity}, " +
            "frozen_quantity = frozen_quantity - #{quantity}, " +
            "inventory_status = 1, " +
            "update_time = NOW() WHERE id = #{id} AND frozen_quantity >= #{quantity}")
    int unfreezeQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_inventory_batch SET inventory_status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM wms_inventory_batch")
    int selectCount();
}
