package com.wms.service.impl;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.dto.PickingConfirmDTO;
import com.wms.entity.PickingTask;
import com.wms.entity.PickingTaskDetail;
import com.wms.entity.ShipmentAllocateDetail;
import com.wms.entity.ShipmentOrder;
import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import com.wms.mapper.PickingTaskMapper;
import com.wms.mapper.ShipmentOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.PickingTaskService;
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
public class PickingTaskServiceImpl implements PickingTaskService {

    @Autowired
    private PickingTaskMapper pickingTaskMapper;

    @Autowired
    private ShipmentOrderMapper shipmentOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RedisLock redisLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> generatePickingTasks(List<Long> shipmentOrderIds, Integer pickingMode,
                                           String picker, Integer priority, String operator) {
        List<Long> taskIds = new ArrayList<>();

        for (Long shipmentOrderId : shipmentOrderIds) {
            String lockKey = "picking:generate:" + shipmentOrderId;
            redisLock.executeWithLock(lockKey, () -> {
                ShipmentOrder order = shipmentOrderMapper.selectById(shipmentOrderId);
                if (order == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXIST, "出库单不存在");
                }
                if (order.getOrderStatus() != 2) {
                    throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "出库单未分配库存，无法生成拣货任务");
                }

                List<ShipmentAllocateDetail> allocateDetails = shipmentOrderMapper.selectAllocateDetailsByOrderId(shipmentOrderId);
                if (allocateDetails == null || allocateDetails.isEmpty()) {
                    throw new BusinessException(ResultCode.ALLOCATE_FAILED, "出库单无分配明细");
                }

                PickingTask task = new PickingTask();
                task.setTaskNo(generateTaskNo());
                task.setShipmentOrderId(shipmentOrderId);
                task.setWarehouseId(order.getWarehouseId());
                task.setPicker(picker);
                task.setTaskType(1);
                task.setPickingMode(pickingMode != null ? pickingMode : 1);
                task.setStatus(1);
                task.setPriority(priority != null ? priority : 3);
                task.setAssignTime(new Date());
                task.setRemark("");
                task.setCreateBy(operator);
                task.setUpdateBy(operator);
                task.setCreateTime(new Date());
                task.setUpdateTime(new Date());

                int totalItems = 0;
                BigDecimal totalQuantity = BigDecimal.ZERO;
                for (ShipmentAllocateDetail allocateDetail : allocateDetails) {
                    BigDecimal remainQty = allocateDetail.getAllocateQuantity().subtract(allocateDetail.getPickedQuantity());
                    if (remainQty.compareTo(BigDecimal.ZERO) > 0) {
                        totalItems++;
                        totalQuantity = totalQuantity.add(remainQty);
                    }
                }
                task.setTotalItems(totalItems);
                task.setTotalQuantity(totalQuantity);
                task.setPickedItems(0);
                task.setPickedQuantity(BigDecimal.ZERO);

                int rows = pickingTaskMapper.insert(task);
                if (rows != 1) {
                    throw new BusinessException(ResultCode.DATABASE_ERROR, "拣货任务创建失败");
                }

                for (ShipmentAllocateDetail allocateDetail : allocateDetails) {
                    BigDecimal remainQty = allocateDetail.getAllocateQuantity().subtract(allocateDetail.getPickedQuantity());
                    if (remainQty.compareTo(BigDecimal.ZERO) > 0) {
                        PickingTaskDetail taskDetail = new PickingTaskDetail();
                        taskDetail.setPickingTaskId(task.getId());
                        taskDetail.setShipmentAllocateId(allocateDetail.getId());
                        taskDetail.setProductId(allocateDetail.getProductId());
                        taskDetail.setBatchNo(allocateDetail.getBatchNo());
                        taskDetail.setLocationId(allocateDetail.getLocationId());
                        taskDetail.setLocationCode("");
                        taskDetail.setPlanQuantity(remainQty);
                        taskDetail.setPickedQuantity(BigDecimal.ZERO);
                        taskDetail.setUnit(allocateDetail.getUnit());
                        taskDetail.setIsPicked(0);
                        taskDetail.setRemark("");
                        taskDetail.setCreateTime(new Date());
                        taskDetail.setUpdateTime(new Date());

                        pickingTaskMapper.insertDetail(taskDetail);
                    }
                }

                order.setOrderStatus(3);
                order.setPickingTime(new Date());
                order.setUpdateBy(operator);
                order.setUpdateTime(new Date());
                shipmentOrderMapper.updateById(order);

                taskIds.add(task.getId());
                log.info("生成拣货任务成功: taskNo={}, shipmentOrderId={}, operator={}",
                        task.getTaskNo(), shipmentOrderId, operator);
                return null;
            });
        }

        return taskIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPicking(PickingConfirmDTO dto, String operator) {
        String lockKey = "picking:confirm:" + dto.getTaskId();
        redisLock.executeWithLock(lockKey, () -> {
            PickingTask task = pickingTaskMapper.selectById(dto.getTaskId());
            if (task == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "拣货任务不存在");
            }
            if (task.getStatus() != 1 && task.getStatus() != 2) {
                throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "当前状态不允许拣货确认");
            }

            for (PickingConfirmDTO.PickingDetailDTO detailDTO : dto.getDetails()) {
                PickingTaskDetail taskDetail = pickingTaskMapper.selectDetailById(detailDTO.getTaskDetailId());
                if (taskDetail == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXIST, "拣货任务明细不存在");
                }
                if (!taskDetail.getPickingTaskId().equals(dto.getTaskId())) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "拣货明细不属于当前任务");
                }
                if (taskDetail.getIsPicked() == 1) {
                    throw new BusinessException(ResultCode.REPEAT_PICKING, "该明细已拣货，请勿重复操作");
                }
                if (detailDTO.getPickedQuantity().compareTo(taskDetail.getPlanQuantity()) > 0) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "拣货数量不能大于计划数量");
                }

                ShipmentAllocateDetail allocateDetail = shipmentOrderMapper.selectAllocateDetailById(
                        taskDetail.getShipmentAllocateId());
                if (allocateDetail == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXIST, "分配明细不存在");
                }

                int rows = pickingTaskMapper.confirmPickingDetail(
                        detailDTO.getTaskDetailId(),
                        detailDTO.getPickedQuantity(),
                        dto.getPicker() != null ? dto.getPicker() : operator
                );
                if (rows != 1) {
                    throw new BusinessException(ResultCode.DATABASE_ERROR, "拣货确认失败");
                }

                shipmentOrderMapper.addPickedQuantityToAllocate(
                        allocateDetail.getId(), detailDTO.getPickedQuantity());

                shipmentOrderMapper.addPickedQuantity(
                        allocateDetail.getShipmentOrderId(), detailDTO.getPickedQuantity());

                pickingTaskMapper.addPickedQuantity(task.getId(), detailDTO.getPickedQuantity());

                log.info("拣货确认成功: taskDetailId={}, pickedQuantity={}, operator={}",
                        detailDTO.getTaskDetailId(), detailDTO.getPickedQuantity(), operator);
            }

            PickingTask updatedTask = pickingTaskMapper.selectById(dto.getTaskId());
            if (updatedTask.getStatus() == 3) {
                ShipmentOrder order = shipmentOrderMapper.selectById(updatedTask.getShipmentOrderId());
                if (order != null && order.getOrderStatus() == 3) {
                    order.setOrderStatus(4);
                    order.setUpdateBy(operator);
                    order.setUpdateTime(new Date());
                    shipmentOrderMapper.updateById(order);
                }
            }

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePicking(Long taskId, String operator) {
        PickingTask task = pickingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "拣货任务不存在");
        }
        if (task.getStatus() == 3) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "拣货任务已完成");
        }

        List<PickingTaskDetail> details = pickingTaskMapper.selectDetailsByTaskId(taskId);
        boolean allPicked = details.stream().allMatch(d -> d.getIsPicked() == 1);
        if (!allPicked) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "还有未拣货的明细，请先完成拣货");
        }

        task.setStatus(3);
        task.setFinishTime(new Date());
        task.setUpdateBy(operator);
        task.setUpdateTime(new Date());

        int rows = pickingTaskMapper.updateById(task);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "拣货任务完成失败");
        }

        ShipmentOrder order = shipmentOrderMapper.selectById(task.getShipmentOrderId());
        if (order != null && order.getOrderStatus() == 3) {
            order.setOrderStatus(4);
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            shipmentOrderMapper.updateById(order);
        }

        log.info("拣货任务完成: taskNo={}, operator={}", task.getTaskNo(), operator);
    }

    @Override
    public PageResult<PickingTask> queryPickingTasks(PageQuery query, Long warehouseId,
                                                     Long shipmentOrderId, String picker,
                                                     Integer status, Integer priority) {
        List<PickingTask> list = pickingTaskMapper.selectList(query, warehouseId,
                shipmentOrderId, picker, status, priority);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public PickingTask getById(Long id) {
        return pickingTaskMapper.selectById(id);
    }

    @Override
    public PickingTask getByNo(String taskNo) {
        return pickingTaskMapper.selectByNo(taskNo);
    }

    @Override
    public List<PickingTaskDetail> getDetailsByTaskId(Long taskId) {
        return pickingTaskMapper.selectDetailsByTaskId(taskId);
    }

    private String generateTaskNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "JH" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
