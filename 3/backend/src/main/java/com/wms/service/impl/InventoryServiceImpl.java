package com.wms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wms.common.BusinessType;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.dto.InventoryQueryDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.InventoryLog;
import com.wms.entity.Location;
import com.wms.entity.Product;
import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import com.wms.mapper.InventoryBatchMapper;
import com.wms.mapper.InventoryLogMapper;
import com.wms.mapper.LocationMapper;
import com.wms.mapper.ProductMapper;
import com.wms.service.InventoryService;
import com.wms.statemachine.InventoryEvent;
import com.wms.statemachine.InventoryState;
import com.wms.statemachine.InventoryStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private InventoryStateMachine inventoryStateMachine;

    @Override
    public PageResult<InventoryBatch> queryInventoryBatch(InventoryQueryDTO query) {
        List<InventoryBatch> list = inventoryBatchMapper.selectList(query);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public PageResult<InventoryLog> queryInventoryLog(PageQuery query, Long warehouseId,
                                                      Long productId, String batchNo,
                                                      Integer businessType, String businessNo) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<InventoryLog> list = inventoryLogMapper.selectList(query, warehouseId, productId,
                batchNo, businessType, businessNo);
        PageInfo<InventoryLog> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(),
                pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long warehouseId, Long locationId, Long productId, String batchNo,
                        Long supplierId, BigDecimal quantity, String unit,
                        Date produceDate, Date expireDate,
                        BigDecimal costPrice, String businessNo, String operator, String remark) {
        if (!checkLocationCapacity(locationId, quantity)) {
            throw new BusinessException(ResultCode.LOCATION_CAPACITY_NOT_ENOUGH);
        }

        String lockKey = "inventory:in:" + warehouseId + ":" + productId + ":" + batchNo;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch existingBatch = inventoryBatchMapper.selectForUpdate(warehouseId, productId, batchNo, locationId);

            if (existingBatch != null) {
                BigDecimal beforeQuantity = existingBatch.getQuantity();
                int rows = inventoryBatchMapper.addQuantity(existingBatch.getId(), quantity);
                if (rows != 1) {
                    throw new BusinessException(ResultCode.DATABASE_ERROR, "库存增加失败");
                }
                recordInventoryLog(existingBatch, BusinessType.INBOUND, businessNo,
                        beforeQuantity, quantity, operator, remark);
            } else {
                Product product = productMapper.selectById(productId);
                if (product == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXIST, "商品不存在");
                }

                InventoryBatch newBatch = new InventoryBatch();
                newBatch.setWarehouseId(warehouseId);
                newBatch.setLocationId(locationId);
                newBatch.setProductId(productId);
                newBatch.setBatchNo(batchNo);
                newBatch.setSupplierId(supplierId);
                newBatch.setQuantity(quantity);
                newBatch.setAvailableQuantity(quantity);
                newBatch.setLockedQuantity(BigDecimal.ZERO);
                newBatch.setFrozenQuantity(BigDecimal.ZERO);
                newBatch.setUnit(unit);
                newBatch.setProduceDate(produceDate);
                newBatch.setExpireDate(expireDate);
                newBatch.setInboundDate(new Date());
                newBatch.setCostPrice(costPrice);
                newBatch.setRemark(remark);
                newBatch.setCreateBy(operator);
                newBatch.setUpdateBy(operator);
                newBatch.setInspectionStatus(2);

                Integer inventoryStatus = 1;
                if (expireDate != null && product.getWarningDays() != null) {
                    long remainingDays = (expireDate.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                    InventoryState state = inventoryStateMachine.getExpireState((int) remainingDays, product.getWarningDays());
                    inventoryStatus = state.getCode();
                }
                newBatch.setInventoryStatus(inventoryStatus);

                int rows = inventoryBatchMapper.insert(newBatch);
                if (rows != 1) {
                    throw new BusinessException(ResultCode.DATABASE_ERROR, "库存批次创建失败");
                }

                recordInventoryLog(newBatch, BusinessType.INBOUND, businessNo,
                        BigDecimal.ZERO, quantity, operator, remark);
            }

            int locationRows = locationMapper.addQuantity(locationId, quantity);
            if (locationRows != 1) {
                throw new BusinessException(ResultCode.LOCATION_CAPACITY_NOT_ENOUGH);
            }

            return null;
        });
    }

    @Override
    public List<InventoryBatch> allocateForOutbound(Long warehouseId, Long productId,
                                                    BigDecimal requiredQuantity,
                                                    Integer outboundStrategy,
                                                    String specifyBatchNo) {
        if (requiredQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "出库数量必须大于0");
        }

        List<InventoryBatch> availableBatches = inventoryBatchMapper.selectByProductIdForOutbound(
                warehouseId, productId, outboundStrategy, specifyBatchNo);

        if (availableBatches == null || availableBatches.isEmpty()) {
            throw new BusinessException(ResultCode.INVENTORY_SHORTAGE);
        }

        BigDecimal totalAvailable = availableBatches.stream()
                .map(InventoryBatch::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(requiredQuantity) < 0) {
            throw new BusinessException(ResultCode.INVENTORY_SHORTAGE,
                    "库存不足，可用：" + totalAvailable + "，需要：" + requiredQuantity);
        }

        List<InventoryBatch> result = new ArrayList<>();
        BigDecimal remaining = requiredQuantity;

        for (InventoryBatch batch : availableBatches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal availableQty = batch.getAvailableQuantity();
            BigDecimal allocateQty = availableQty.min(remaining);

            if (allocateQty.compareTo(BigDecimal.ZERO) > 0) {
                InventoryBatch allocateBatch = new InventoryBatch();
                allocateBatch.setId(batch.getId());
                allocateBatch.setWarehouseId(batch.getWarehouseId());
                allocateBatch.setLocationId(batch.getLocationId());
                allocateBatch.setProductId(batch.getProductId());
                allocateBatch.setBatchNo(batch.getBatchNo());
                allocateBatch.setSupplierId(batch.getSupplierId());
                allocateBatch.setQuantity(allocateQty);
                allocateBatch.setAvailableQuantity(availableQty);
                allocateBatch.setUnit(batch.getUnit());
                allocateBatch.setProduceDate(batch.getProduceDate());
                allocateBatch.setExpireDate(batch.getExpireDate());
                allocateBatch.setInboundDate(batch.getInboundDate());
                allocateBatch.setInventoryStatus(batch.getInventoryStatus());
                allocateBatch.setCostPrice(batch.getCostPrice());

                result.add(allocateBatch);
                remaining = remaining.subtract(allocateQty);
            }
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(ResultCode.ALLOCATE_FAILED, "库存分配失败，可用库存不足");
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockInventory(Long batchId, BigDecimal quantity, String businessNo, String operator) {
        String lockKey = "inventory:lock:" + batchId;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch batch = inventoryBatchMapper.selectById(batchId);
            if (batch == null) {
                throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
            }

            InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
            inventoryStateMachine.transition(currentState, InventoryEvent.LOCK);

            BigDecimal beforeQuantity = batch.getAvailableQuantity();
            int rows = inventoryBatchMapper.lockQuantity(batchId, quantity);
            if (rows != 1) {
                throw new BusinessException(ResultCode.INVENTORY_LOCKED, "库存锁定失败，可用库存不足");
            }

            batch.setAvailableQuantity(beforeQuantity.subtract(quantity));
            batch.setLockedQuantity(batch.getLockedQuantity().add(quantity));

            recordInventoryLog(batch, BusinessType.INBOUND, businessNo,
                    beforeQuantity, quantity.negate(), operator, "锁定库存");

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockInventory(Long batchId, BigDecimal quantity, String businessNo, String operator) {
        String lockKey = "inventory:unlock:" + batchId;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch batch = inventoryBatchMapper.selectById(batchId);
            if (batch == null) {
                throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
            }

            InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
            if (currentState == InventoryState.LOCKED) {
                inventoryStateMachine.transition(currentState, InventoryEvent.UNLOCK);
            }

            BigDecimal beforeQuantity = batch.getAvailableQuantity();
            int rows = inventoryBatchMapper.unlockQuantity(batchId, quantity);
            if (rows != 1) {
                throw new BusinessException(ResultCode.INVENTORY_LOCKED, "库存解锁失败，锁定库存不足");
            }

            batch.setAvailableQuantity(beforeQuantity.add(quantity));
            batch.setLockedQuantity(batch.getLockedQuantity().subtract(quantity));

            recordInventoryLog(batch, BusinessType.INBOUND, businessNo,
                    beforeQuantity, quantity, operator, "解锁库存");

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceInventory(Long batchId, BigDecimal quantity, String businessNo, String operator) {
        String lockKey = "inventory:reduce:" + batchId;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch batch = inventoryBatchMapper.selectForUpdate(
                    batch.getWarehouseId(), batch.getProductId(), batch.getBatchNo(), batch.getLocationId());
            if (batch == null) {
                throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
            }

            InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
            inventoryStateMachine.transition(currentState, InventoryEvent.OUTBOUND);

            BigDecimal beforeQuantity = batch.getQuantity();
            int rows = inventoryBatchMapper.reduceQuantity(batchId, quantity);
            if (rows != 1) {
                throw new BusinessException(ResultCode.REDUCE_FAILED, "库存扣减失败，可用库存不足");
            }

            batch.setQuantity(beforeQuantity.subtract(quantity));
            batch.setAvailableQuantity(batch.getAvailableQuantity().subtract(quantity));

            recordInventoryLog(batch, BusinessType.OUTBOUND, businessNo,
                    beforeQuantity, quantity.negate(), operator, "库存扣减");

            int locationRows = locationMapper.reduceQuantity(batch.getLocationId(), quantity);
            if (locationRows != 1) {
                throw new BusinessException(ResultCode.DATABASE_ERROR, "库位数量更新失败");
            }

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceLockedInventory(Long batchId, BigDecimal quantity, String businessNo, String operator) {
        String lockKey = "inventory:reduceLocked:" + batchId;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch batch = inventoryBatchMapper.selectById(batchId);
            if (batch == null) {
                throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
            }

            InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
            inventoryStateMachine.transition(currentState, InventoryEvent.OUTBOUND);

            BigDecimal beforeQuantity = batch.getQuantity();
            int rows = inventoryBatchMapper.reduceLockedQuantity(batchId, quantity);
            if (rows != 1) {
                throw new BusinessException(ResultCode.REDUCE_FAILED, "锁定库存扣减失败，锁定库存不足");
            }

            batch.setQuantity(beforeQuantity.subtract(quantity));
            batch.setLockedQuantity(batch.getLockedQuantity().subtract(quantity));

            recordInventoryLog(batch, BusinessType.OUTBOUND, businessNo,
                    beforeQuantity, quantity.negate(), operator, "锁定库存扣减");

            int locationRows = locationMapper.reduceQuantity(batch.getLocationId(), quantity);
            if (locationRows != 1) {
                throw new BusinessException(ResultCode.DATABASE_ERROR, "库位数量更新失败");
            }

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeInventory(Long batchId, BigDecimal quantity, String businessNo, String operator) {
        String lockKey = "inventory:freeze:" + batchId;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch batch = inventoryBatchMapper.selectById(batchId);
            if (batch == null) {
                throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
            }

            InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
            inventoryStateMachine.transition(currentState, InventoryEvent.FREEZE);

            BigDecimal beforeQuantity = batch.getAvailableQuantity();
            int rows = inventoryBatchMapper.freezeQuantity(batchId, quantity);
            if (rows != 1) {
                throw new BusinessException(ResultCode.INVENTORY_FROZEN, "库存冻结失败，可用库存不足");
            }

            batch.setAvailableQuantity(beforeQuantity.subtract(quantity));
            batch.setFrozenQuantity(batch.getFrozenQuantity().add(quantity));

            recordInventoryLog(batch, BusinessType.FREEZE, businessNo,
                    beforeQuantity, quantity.negate(), operator, "冻结库存");

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeInventory(Long batchId, BigDecimal quantity, String businessNo, String operator) {
        String lockKey = "inventory:unfreeze:" + batchId;
        redisLock.executeWithLock(lockKey, () -> {
            InventoryBatch batch = inventoryBatchMapper.selectById(batchId);
            if (batch == null) {
                throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
            }

            InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
            inventoryStateMachine.transition(currentState, InventoryEvent.UNFREEZE);

            BigDecimal beforeQuantity = batch.getAvailableQuantity();
            int rows = inventoryBatchMapper.unfreezeQuantity(batchId, quantity);
            if (rows != 1) {
                throw new BusinessException(ResultCode.INVENTORY_FROZEN, "库存解冻失败，冻结库存不足");
            }

            batch.setAvailableQuantity(beforeQuantity.add(quantity));
            batch.setFrozenQuantity(batch.getFrozenQuantity().subtract(quantity));

            recordInventoryLog(batch, BusinessType.UNFREEZE, businessNo,
                    beforeQuantity, quantity, operator, "解冻库存");

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInventoryStatus(Long batchId, Integer targetStatus, String operator) {
        InventoryBatch batch = inventoryBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException(ResultCode.BATCH_NOT_EXIST);
        }

        InventoryState currentState = InventoryState.fromCode(batch.getInventoryStatus());
        InventoryState targetState = InventoryState.fromCode(targetStatus);

        if (targetState == InventoryState.EXPIRED && currentState != InventoryState.EXPIRED) {
            inventoryStateMachine.transition(currentState, InventoryEvent.EXPIRE);
        }

        int rows = inventoryBatchMapper.updateStatus(batchId, targetStatus);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "库存状态更新失败");
        }

        log.info("库存状态更新: batchId={}, 状态: {} -> {}, 操作人: {}", batchId, currentState.getName(), targetState.getName(), operator);
    }

    @Override
    public List<InventoryLog> queryBatchTrace(String batchNo, Long warehouseId, Long productId) {
        return inventoryLogMapper.selectList(null, warehouseId, productId, batchNo, null, null);
    }

    @Override
    public boolean checkLocationCapacity(Long locationId, BigDecimal quantity) {
        Location location = locationMapper.selectById(locationId);
        if (location == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "库位不存在");
        }
        return location.getAvailableCapacity().compareTo(quantity) >= 0;
    }

    @Override
    public InventoryBatch getById(Long id) {
        return inventoryBatchMapper.selectById(id);
    }

    private void recordInventoryLog(InventoryBatch batch, BusinessType businessType,
                                    String businessNo, BigDecimal beforeQuantity,
                                    BigDecimal changeQuantity, String operator, String remark) {
        InventoryLog log = new InventoryLog();
        log.setLogNo(generateLogNo());
        log.setWarehouseId(batch.getWarehouseId());
        log.setLocationId(batch.getLocationId());
        log.setProductId(batch.getProductId());
        log.setBatchNo(batch.getBatchNo());
        log.setBusinessType(businessType.getCode());
        log.setBusinessNo(businessNo);
        log.setBeforeQuantity(beforeQuantity);
        log.setChangeQuantity(changeQuantity);
        log.setAfterQuantity(beforeQuantity.add(changeQuantity));
        log.setUnit(batch.getUnit());
        log.setOperationType(changeQuantity.compareTo(BigDecimal.ZERO) >= 0 ? 1 : 2);
        log.setOperator(operator);
        log.setOperationTime(new Date());
        log.setRemark(remark);
        log.setCreateTime(new Date());

        inventoryLogMapper.insert(log);
    }

    private String generateLogNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "LOG" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
