package com.wms;

import com.wms.common.OutboundStrategy;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.InventoryQueryDTO;
import com.wms.dto.ShipmentOrderCreateDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.ShipmentAllocateDetail;
import com.wms.entity.ShipmentOrder;
import com.wms.entity.ShipmentOrderDetail;
import com.wms.exception.BusinessException;
import com.wms.mapper.ShipmentOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.ShipmentOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@Rollback
@DisplayName("出库流程测试")
public class ShipmentOrderServiceTest {

    @Autowired
    private ShipmentOrderService shipmentOrderService;

    @Autowired
    private ShipmentOrderMapper shipmentOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    private Long testWarehouseId;
    private Long testLocationId;
    private Long testProductId;
    private String operator;

    @BeforeEach
    public void setUp() {
        testWarehouseId = 1L;
        testLocationId = 1L;
        testProductId = 1L;
        operator = "testAdmin";
        log.info("初始化出库测试数据: warehouseId={}, productId={}", testWarehouseId, testProductId);
    }

    @Test
    @DisplayName("测试创建出库单")
    public void testCreateShipmentOrder() {
        log.info("开始测试创建出库单");

        prepareStock(100);

        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);

        assertNotNull(orderId, "创建出库单应返回订单ID");
        log.info("创建出库单成功，订单ID: {}", orderId);

        ShipmentOrder order = shipmentOrderService.getById(orderId);
        assertNotNull(order);
        assertEquals(1, order.getOrderStatus(), "新建出库单状态应为待确认(1)");
        assertEquals(testWarehouseId, order.getWarehouseId());
        assertEquals(0, new BigDecimal("30").compareTo(order.getTotalQuantity()));
        log.info("出库单信息验证成功: 单号={}, 状态={}", order.getShipmentNo(), order.getOrderStatus());

        List<ShipmentOrderDetail> details = shipmentOrderService.getDetailsByOrderId(orderId);
        assertNotNull(details);
        assertFalse(details.isEmpty());
        assertEquals(1, details.size());
        assertEquals(testProductId, details.get(0).getProductId());
        assertEquals(0, new BigDecimal("30").compareTo(details.get(0).getPlanQuantity()));
        log.info("出库单明细验证成功，明细数量: {}", details.size());
    }

    @Test
    @DisplayName("测试库存自动分配")
    public void testAllocateInventory() {
        log.info("开始测试库存自动分配");

        prepareStock(100);

        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);

        shipmentOrderService.allocateInventory(orderId, operator);

        ShipmentOrder order = shipmentOrderService.getById(orderId);
        assertEquals(2, order.getOrderStatus(), "分配后状态应为已确认(2)");
        assertNotNull(order.getAllocateTime());
        log.info("库存分配成功，状态: {}, 分配时间: {}", order.getOrderStatus(), order.getAllocateTime());

        List<ShipmentAllocateDetail> allocateDetails = shipmentOrderService.getAllocateDetailsByOrderId(orderId);
        assertNotNull(allocateDetails);
        assertFalse(allocateDetails.isEmpty());

        BigDecimal totalAllocated = allocateDetails.stream()
                .map(ShipmentAllocateDetail::getAllocateQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("30").compareTo(totalAllocated), "分配总数量应等于计划数量");
        log.info("库存分配明细验证成功，分配总数量: {}", totalAllocated);

        List<ShipmentOrderDetail> orderDetails = shipmentOrderService.getDetailsByOrderId(orderId);
        for (ShipmentOrderDetail detail : orderDetails) {
            assertEquals(0, detail.getPlanQuantity().compareTo(detail.getAllocatedQuantity()));
        }
        log.info("库存自动分配测试完成");
    }

    @Test
    @DisplayName("测试出库撤销 - 回滚库存")
    public void testCancelShipment() {
        log.info("开始测试出库撤销");

        String batchNo = "CANCEL-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);

        shipmentOrderService.allocateInventory(orderId, operator);

        InventoryBatch beforeCancel = getBatchByNo(batchNo);
        BigDecimal beforeAvailable = beforeCancel.getAvailableQuantity();
        BigDecimal beforeLocked = beforeCancel.getLockedQuantity();
        log.info("撤销前: 可用={}, 锁定={}", beforeAvailable, beforeLocked);

        shipmentOrderService.cancelShipment(orderId, "测试撤销", operator);

        ShipmentOrder order = shipmentOrderService.getById(orderId);
        assertEquals(8, order.getOrderStatus(), "撤销后状态应为已取消(8)");
        assertNotNull(order.getCancelTime());
        assertEquals("测试撤销", order.getCancelReason());
        log.info("出库撤销成功，状态: {}", order.getOrderStatus());

        InventoryBatch afterCancel = getBatchByNo(batchNo);
        BigDecimal afterAvailable = afterCancel.getAvailableQuantity();
        BigDecimal afterLocked = afterCancel.getLockedQuantity();
        log.info("撤销后: 可用={}, 锁定={}", afterAvailable, afterLocked);

        assertEquals(0, beforeAvailable.add(new BigDecimal("30")).compareTo(afterAvailable),
                "撤销后可用库存应增加30");
        assertEquals(0, beforeLocked.subtract(new BigDecimal("30")).compareTo(afterLocked),
                "撤销后锁定库存应减少30");
        log.info("出库撤销回滚库存测试完成");
    }

    @Test
    @DisplayName("测试重复拣货防护")
    public void testRepeatPickingProtection() {
        log.info("开始测试重复拣货防护");

        prepareStock(100);

        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);
        shipmentOrderService.allocateInventory(orderId, operator);

        List<ShipmentAllocateDetail> allocateDetails = shipmentOrderService.getAllocateDetailsByOrderId(orderId);
        assertFalse(allocateDetails.isEmpty());
        Long allocateId = allocateDetails.get(0).getId();
        BigDecimal allocateQty = allocateDetails.get(0).getAllocateQuantity();

        int updateCount = shipmentOrderMapper.addPickedQuantityToAllocate(allocateId, allocateQty);
        assertEquals(1, updateCount, "第一次拣货应该成功");
        log.info("第一次拣货成功，拣货数量: {}", allocateQty);

        ShipmentAllocateDetail afterFirstPick = shipmentOrderMapper.selectAllocateDetailById(allocateId);
        assertEquals(1, afterFirstPick.getIsPicked(), "拣货完成标记应为1");
        log.info("拣货完成标记: {}", afterFirstPick.getIsPicked());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            shipmentOrderService.allocateInventory(orderId, operator);
        }, "已分配的出库单重复分配应抛出异常");

        assertNotNull(exception);
        log.info("重复拣货防护成功，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试出库完成确认")
    public void testConfirmShipmentComplete() {
        log.info("开始测试出库完成确认");

        String batchNo = "COMPLETE-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);
        shipmentOrderService.allocateInventory(orderId, operator);

        InventoryBatch beforeComplete = getBatchByNo(batchNo);
        BigDecimal beforeQuantity = beforeComplete.getQuantity();
        BigDecimal beforeLocked = beforeComplete.getLockedQuantity();
        log.info("出库完成前: 总库存={}, 锁定={}", beforeQuantity, beforeLocked);

        shipmentOrderService.confirmShipmentComplete(orderId, operator);

        ShipmentOrder order = shipmentOrderService.getById(orderId);
        assertEquals(7, order.getOrderStatus(), "出库完成后状态应为已出库(7)");
        assertNotNull(order.getShipmentTime());
        assertNotNull(order.getActualQuantity());
        log.info("出库完成确认成功，状态: {}, 出库时间: {}", order.getOrderStatus(), order.getShipmentTime());

        InventoryBatch afterComplete = getBatchByNo(batchNo);
        BigDecimal afterQuantity = afterComplete.getQuantity();
        BigDecimal afterLocked = afterComplete.getLockedQuantity();
        log.info("出库完成后: 总库存={}, 锁定={}", afterQuantity, afterLocked);

        assertEquals(0, beforeQuantity.subtract(new BigDecimal("30")).compareTo(afterQuantity),
                "出库完成后总库存应减少30");
        assertEquals(0, beforeLocked.subtract(new BigDecimal("30")).compareTo(afterLocked),
                "出库完成后锁定库存应减少30");
        log.info("出库完成确认测试完成");
    }

    @Test
    @DisplayName("测试完整出库流程 - 从创建到完成")
    public void testCompleteShipmentFlow() {
        log.info("开始测试完整出库流程");

        String batchNo = "FULL-SHIP-" + System.currentTimeMillis();
        String operator = "admin";
        prepareStockWithBatch(batchNo, 100);

        log.info("步骤1: 创建出库单");
        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);
        ShipmentOrder order = shipmentOrderService.getById(orderId);
        assertEquals(1, order.getOrderStatus());
        log.info("创建出库单成功: {}，状态: {}", order.getShipmentNo(), order.getOrderStatus());

        log.info("步骤2: 库存自动分配");
        shipmentOrderService.allocateInventory(orderId, operator);
        order = shipmentOrderService.getById(orderId);
        assertEquals(2, order.getOrderStatus());
        log.info("库存分配完成，状态: {}", order.getOrderStatus());

        InventoryBatch afterAllocate = getBatchByNo(batchNo);
        assertEquals(0, new BigDecimal("70").compareTo(afterAllocate.getAvailableQuantity()));
        assertEquals(0, new BigDecimal("30").compareTo(afterAllocate.getLockedQuantity()));
        log.info("分配后库存: 可用={}, 锁定={}", afterAllocate.getAvailableQuantity(), afterAllocate.getLockedQuantity());

        log.info("步骤3: 出库完成确认");
        shipmentOrderService.confirmShipmentComplete(orderId, operator);
        order = shipmentOrderService.getById(orderId);
        assertEquals(7, order.getOrderStatus());
        assertNotNull(order.getShipmentTime());
        log.info("出库完成，状态: {}, 出库时间: {}", order.getOrderStatus(), order.getShipmentTime());

        InventoryBatch afterShip = getBatchByNo(batchNo);
        assertEquals(0, new BigDecimal("70").compareTo(afterShip.getQuantity()));
        assertEquals(0, new BigDecimal("70").compareTo(afterShip.getAvailableQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(afterShip.getLockedQuantity()));
        log.info("出库后库存: 总库存={}, 可用={}, 锁定={}",
                afterShip.getQuantity(), afterShip.getAvailableQuantity(), afterShip.getLockedQuantity());

        log.info("完整出库流程测试完成");
    }

    @Test
    @DisplayName("测试库存不足时分配失败")
    public void testAllocateWithInsufficientStock() {
        log.info("开始测试库存不足时分配失败");

        prepareStock(20);

        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("50"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            shipmentOrderService.allocateInventory(orderId, operator);
        }, "库存不足时分配应抛出异常");

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("不足") || exception.getMessage().contains("库存"));
        log.info("库存不足分配失败测试完成，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试出库单查询")
    public void testQueryShipmentOrders() {
        log.info("开始测试出库单查询");

        prepareStock(100);
        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        PageResult<ShipmentOrder> result = shipmentOrderService.queryShipmentOrders(
                pageQuery, 1, 1, testWarehouseId, null, null, null);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getList());
        assertFalse(result.getList().isEmpty());

        boolean found = result.getList().stream()
                .anyMatch(order -> order.getId().equals(orderId));
        assertTrue(found, "查询结果应包含刚创建的出库单");

        log.info("出库单查询测试完成，查询到 {} 条记录", result.getTotal());
    }

    @Test
    @DisplayName("测试根据单号查询出库单")
    public void testGetByNo() {
        log.info("开始测试根据单号查询出库单");

        prepareStock(100);
        ShipmentOrderCreateDTO dto = buildCreateDTO(new BigDecimal("30"));
        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);
        ShipmentOrder order = shipmentOrderService.getById(orderId);

        ShipmentOrder found = shipmentOrderService.getByNo(order.getShipmentNo());
        assertNotNull(found);
        assertEquals(orderId, found.getId());
        assertEquals(order.getShipmentNo(), found.getShipmentNo());
        log.info("根据单号查询出库单成功，单号: {}", found.getShipmentNo());
    }

    @Test
    @DisplayName("测试多明细出库单分配")
    public void testMultiDetailShipmentAllocation() {
        log.info("开始测试多明细出库单分配");

        String batchNo1 = "MULTI-1-" + System.currentTimeMillis();
        String batchNo2 = "MULTI-2-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo1, 100);
        prepareStockWithBatchAndProduct(batchNo2, 50, 2L);

        ShipmentOrderCreateDTO dto = new ShipmentOrderCreateDTO();
        dto.setShipmentType(1);
        dto.setWarehouseId(testWarehouseId);
        dto.setCustomerName("测试客户");
        dto.setRemark("多明细出库单测试");

        List<ShipmentOrderCreateDTO.ShipmentDetailDTO> details = new ArrayList<>();

        ShipmentOrderCreateDTO.ShipmentDetailDTO detail1 = new ShipmentOrderCreateDTO.ShipmentDetailDTO();
        detail1.setProductId(1L);
        detail1.setPlanQuantity(new BigDecimal("30"));
        detail1.setUnit("台");
        detail1.setOutboundStrategy(OutboundStrategy.FIFO.getCode());
        detail1.setPrice(new BigDecimal("199.00"));
        details.add(detail1);

        ShipmentOrderCreateDTO.ShipmentDetailDTO detail2 = new ShipmentOrderCreateDTO.ShipmentDetailDTO();
        detail2.setProductId(2L);
        detail2.setPlanQuantity(new BigDecimal("20"));
        detail2.setUnit("件");
        detail2.setOutboundStrategy(OutboundStrategy.FEFO.getCode());
        detail2.setPrice(new BigDecimal("299.00"));
        details.add(detail2);

        dto.setDetails(details);

        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);
        shipmentOrderService.allocateInventory(orderId, operator);

        List<ShipmentAllocateDetail> allocateDetails = shipmentOrderService.getAllocateDetailsByOrderId(orderId);
        assertTrue(allocateDetails.size() >= 2);

        BigDecimal totalAllocated = allocateDetails.stream()
                .map(ShipmentAllocateDetail::getAllocateQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("50").compareTo(totalAllocated));

        log.info("多明细出库单分配测试完成，分配明细数量: {}, 总分配数量: {}",
                allocateDetails.size(), totalAllocated);
    }

    @Test
    @DisplayName("测试指定批次出库策略")
    public void testSpecifyBatchOutbound() {
        log.info("开始测试指定批次出库策略");

        String batchNo1 = "SPECIFY-1-" + System.currentTimeMillis();
        String batchNo2 = "SPECIFY-2-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo1, 50);
        prepareStockWithBatch(batchNo2, 50);

        ShipmentOrderCreateDTO dto = new ShipmentOrderCreateDTO();
        dto.setShipmentType(1);
        dto.setWarehouseId(testWarehouseId);
        dto.setCustomerName("指定批次客户");

        List<ShipmentOrderCreateDTO.ShipmentDetailDTO> details = new ArrayList<>();
        ShipmentOrderCreateDTO.ShipmentDetailDTO detail = new ShipmentOrderCreateDTO.ShipmentDetailDTO();
        detail.setProductId(testProductId);
        detail.setPlanQuantity(new BigDecimal("20"));
        detail.setUnit("台");
        detail.setOutboundStrategy(OutboundStrategy.SPECIFY_BATCH.getCode());
        detail.setSpecifyBatchNo(batchNo1);
        details.add(detail);
        dto.setDetails(details);

        Long orderId = shipmentOrderService.createShipmentOrder(dto, operator);
        shipmentOrderService.allocateInventory(orderId, operator);

        List<ShipmentAllocateDetail> allocateDetails = shipmentOrderService.getAllocateDetailsByOrderId(orderId);
        assertFalse(allocateDetails.isEmpty());
        assertEquals(batchNo1, allocateDetails.get(0).getBatchNo(), "应只从指定批次分配");
        log.info("指定批次出库策略测试完成，从批次 {} 分配数量 {}",
                allocateDetails.get(0).getBatchNo(), allocateDetails.get(0).getAllocateQuantity());
    }

    private void prepareStock(int quantity) {
        prepareStockWithBatch("STOCK-" + System.currentTimeMillis(), quantity);
    }

    private void prepareStockWithBatch(String batchNo, int quantity) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        inventoryService.stockIn(testWarehouseId, testLocationId, testProductId, batchNo,
                1L, new BigDecimal(quantity), "台", new Date(), cal.getTime(),
                new BigDecimal("100"), "INIT-" + System.currentTimeMillis(), "testAdmin", "初始化库存");
    }

    private void prepareStockWithBatchAndProduct(String batchNo, int quantity, Long productId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        inventoryService.stockIn(testWarehouseId, testLocationId, productId, batchNo,
                1L, new BigDecimal(quantity), "台", new Date(), cal.getTime(),
                new BigDecimal("100"), "INIT-" + System.currentTimeMillis(), "testAdmin", "初始化库存");
    }

    private ShipmentOrderCreateDTO buildCreateDTO(BigDecimal quantity) {
        ShipmentOrderCreateDTO dto = new ShipmentOrderCreateDTO();
        dto.setShipmentType(1);
        dto.setWarehouseId(testWarehouseId);
        dto.setCustomerName("测试客户");
        dto.setSourceOrderNo("SO-" + System.currentTimeMillis());
        dto.setRemark("测试出库单");

        List<ShipmentOrderCreateDTO.ShipmentDetailDTO> details = new ArrayList<>();
        ShipmentOrderCreateDTO.ShipmentDetailDTO detail = new ShipmentOrderCreateDTO.ShipmentDetailDTO();
        detail.setProductId(testProductId);
        detail.setPlanQuantity(quantity);
        detail.setUnit("台");
        detail.setOutboundStrategy(OutboundStrategy.FIFO.getCode());
        detail.setPrice(new BigDecimal("199.00"));
        detail.setRemark("测试明细");
        details.add(detail);
        dto.setDetails(details);

        return dto;
    }

    private InventoryBatch getBatchByNo(String batchNo) {
        InventoryQueryDTO query = new InventoryQueryDTO();
        query.setWarehouseId(testWarehouseId);
        query.setProductId(testProductId);
        query.setBatchNo(batchNo);
        query.setPageNum(1);
        query.setPageSize(10);
        PageResult<InventoryBatch> result = inventoryService.queryInventoryBatch(query);
        if (result != null && result.getList() != null && !result.getList().isEmpty()) {
            return result.getList().get(0);
        }
        return null;
    }
}
