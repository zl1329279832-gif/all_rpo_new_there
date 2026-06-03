package com.wms;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.ReceiptOrderCreateDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.ReceiptOrder;
import com.wms.entity.ReceiptOrderDetail;
import com.wms.mapper.InventoryBatchMapper;
import com.wms.mapper.ReceiptOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.ReceiptOrderService;
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
@DisplayName("入库流程测试")
public class ReceiptOrderServiceTest {

    @Autowired
    private ReceiptOrderService receiptOrderService;

    @Autowired
    private ReceiptOrderMapper receiptOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;

    private Long testWarehouseId;
    private Long testLocationId;
    private Long testProductId;
    private Long testSupplierId;
    private String operator;

    @BeforeEach
    public void setUp() {
        testWarehouseId = 1L;
        testLocationId = 1L;
        testProductId = 1L;
        testSupplierId = 1L;
        operator = "testAdmin";
        log.info("初始化入库测试数据: warehouseId={}, supplierId={}", testWarehouseId, testSupplierId);
    }

    @Test
    @DisplayName("测试创建入库单")
    public void testCreateReceiptOrder() {
        log.info("开始测试创建入库单");

        ReceiptOrderCreateDTO dto = buildCreateDTO();

        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);

        assertNotNull(orderId, "创建入库单应返回订单ID");
        log.info("创建入库单成功，订单ID: {}", orderId);

        ReceiptOrder order = receiptOrderService.getById(orderId);
        assertNotNull(order);
        assertEquals(1, order.getOrderStatus(), "新建入库单状态应为待到货(1)");
        assertEquals(testWarehouseId, order.getWarehouseId());
        assertEquals(testSupplierId, order.getSupplierId());
        assertEquals(0, new BigDecimal("100").compareTo(order.getTotalQuantity()));
        log.info("入库单信息验证成功: 单号={}, 状态={}", order.getReceiptNo(), order.getOrderStatus());

        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        assertNotNull(details);
        assertFalse(details.isEmpty());
        assertEquals(1, details.size());
        assertEquals(testProductId, details.get(0).getProductId());
        assertEquals(0, new BigDecimal("100").compareTo(details.get(0).getPlanQuantity()));
        log.info("入库单明细验证成功，明细数量: {}", details.size());
    }

    @Test
    @DisplayName("测试供应商到货确认")
    public void testConfirmArrival() {
        log.info("开始测试供应商到货确认");

        ReceiptOrderCreateDTO dto = buildCreateDTO();
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);

        Date arrivalTime = new Date();
        receiptOrderService.confirmArrival(orderId, arrivalTime, operator);

        ReceiptOrder order = receiptOrderService.getById(orderId);
        assertNotNull(order);
        assertEquals(2, order.getOrderStatus(), "到货确认后状态应为已到货(2)");
        assertNotNull(order.getArrivalTime());
        log.info("到货确认成功，状态: {}, 到货时间: {}", order.getOrderStatus(), order.getArrivalTime());

        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        assertNotNull(details);
        assertFalse(details.isEmpty());
        ReceiptOrderDetail detail = details.get(0);
        assertNotNull(detail.getArrivalQuantity());
        log.info("到货明细更新成功，到货数量: {}", detail.getArrivalQuantity());
    }

    @Test
    @DisplayName("测试入库质检 - 合格")
    public void testDoInspectionQualified() {
        log.info("开始测试入库质检 - 合格");

        ReceiptOrderCreateDTO dto = buildCreateDTO();
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        receiptOrderService.confirmArrival(orderId, new Date(), operator);

        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();
        BigDecimal arrivalQuantity = new BigDecimal("100");
        BigDecimal qualifiedQuantity = new BigDecimal("100");
        BigDecimal unqualifiedQuantity = BigDecimal.ZERO;

        receiptOrderService.doInspection(detailId, arrivalQuantity, qualifiedQuantity,
                unqualifiedQuantity, 1, "质检合格", operator);

        ReceiptOrderDetail updatedDetail = receiptOrderMapper.selectDetailById(detailId);
        assertNotNull(updatedDetail);
        assertEquals(0, qualifiedQuantity.compareTo(updatedDetail.getQualifiedQuantity()));
        assertEquals(0, unqualifiedQuantity.compareTo(updatedDetail.getUnqualifiedQuantity()));
        assertEquals(1, updatedDetail.getInspectionResult(), "质检结果应为合格(1)");
        log.info("质检合格测试完成，合格数量: {}, 质检结果: {}",
                updatedDetail.getQualifiedQuantity(), updatedDetail.getInspectionResult());

        ReceiptOrder order = receiptOrderService.getById(orderId);
        assertEquals(0, qualifiedQuantity.compareTo(order.getQualifiedQuantity()));
        log.info("入库单质检统计更新成功，总合格数量: {}", order.getQualifiedQuantity());
    }

    @Test
    @DisplayName("测试入库质检 - 不合格")
    public void testDoInspectionUnqualified() {
        log.info("开始测试入库质检 - 不合格");

        ReceiptOrderCreateDTO dto = buildCreateDTO();
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        receiptOrderService.confirmArrival(orderId, new Date(), operator);

        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();
        BigDecimal arrivalQuantity = new BigDecimal("100");
        BigDecimal qualifiedQuantity = new BigDecimal("80");
        BigDecimal unqualifiedQuantity = new BigDecimal("20");

        receiptOrderService.doInspection(detailId, arrivalQuantity, qualifiedQuantity,
                unqualifiedQuantity, 2, "部分不合格", operator);

        ReceiptOrderDetail updatedDetail = receiptOrderMapper.selectDetailById(detailId);
        assertNotNull(updatedDetail);
        assertEquals(0, qualifiedQuantity.compareTo(updatedDetail.getQualifiedQuantity()));
        assertEquals(0, unqualifiedQuantity.compareTo(updatedDetail.getUnqualifiedQuantity()));
        assertEquals(2, updatedDetail.getInspectionResult(), "质检结果应为不合格(2)");
        log.info("质检不合格测试完成，合格数量: {}, 不合格数量: {}",
                updatedDetail.getQualifiedQuantity(), updatedDetail.getUnqualifiedQuantity());
    }

    @Test
    @DisplayName("测试库位分配上架")
    public void testAssignLocation() {
        log.info("开始测试库位分配上架");

        ReceiptOrderCreateDTO dto = buildCreateDTO();
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        receiptOrderService.confirmArrival(orderId, new Date(), operator);

        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();

        receiptOrderService.doInspection(detailId, new BigDecimal("100"), new BigDecimal("100"),
                BigDecimal.ZERO, 1, "质检合格", operator);

        receiptOrderService.assignLocation(detailId, testLocationId, operator);

        ReceiptOrderDetail updatedDetail = receiptOrderMapper.selectDetailById(detailId);
        assertNotNull(updatedDetail);
        assertEquals(testLocationId, updatedDetail.getLocationId());
        log.info("库位分配成功，分配库位ID: {}", updatedDetail.getLocationId());

        ReceiptOrder order = receiptOrderService.getById(orderId);
        assertEquals(5, order.getOrderStatus(), "分配库位后状态应为待入库(5)");
        log.info("入库单状态更新成功，状态: {}", order.getOrderStatus());
    }

    @Test
    @DisplayName("测试入库完成确认")
    public void testConfirmReceiptComplete() {
        log.info("开始测试入库完成确认");

        String batchNo = "RCV-" + System.currentTimeMillis();
        ReceiptOrderCreateDTO dto = buildCreateDTOWithBatch(batchNo);
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        receiptOrderService.confirmArrival(orderId, new Date(), operator);

        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();

        receiptOrderService.doInspection(detailId, new BigDecimal("100"), new BigDecimal("100"),
                BigDecimal.ZERO, 1, "质检合格", operator);
        receiptOrderService.assignLocation(detailId, testLocationId, operator);

        receiptOrderService.confirmReceiptComplete(orderId, operator);

        ReceiptOrder order = receiptOrderService.getById(orderId);
        assertNotNull(order);
        assertEquals(7, order.getOrderStatus(), "入库完成后状态应为已完成(7)");
        assertNotNull(order.getCompleteTime());
        assertNotNull(order.getActualQuantity());
        log.info("入库完成确认成功，状态: {}, 完成时间: {}", order.getOrderStatus(), order.getCompleteTime());

        InventoryBatch batch = getBatchByNo(batchNo);
        assertNotNull(batch, "入库完成后应创建库存批次");
        assertEquals(0, new BigDecimal("100").compareTo(batch.getQuantity()));
        assertEquals(testLocationId, batch.getLocationId());
        assertEquals(testProductId, batch.getProductId());
        log.info("库存批次创建成功，批次号: {}, 数量: {}", batchNo, batch.getQuantity());
    }

    @Test
    @DisplayName("测试入库单查询")
    public void testQueryReceiptOrders() {
        log.info("开始测试入库单查询");

        ReceiptOrderCreateDTO dto = buildCreateDTO();
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        PageResult<ReceiptOrder> result = receiptOrderService.queryReceiptOrders(
                pageQuery, 1, 1, testWarehouseId, testSupplierId, null, null);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getList());
        assertFalse(result.getList().isEmpty());

        boolean found = result.getList().stream()
                .anyMatch(order -> order.getId().equals(orderId));
        assertTrue(found, "查询结果应包含刚创建的入库单");

        log.info("入库单查询测试完成，查询到 {} 条记录", result.getTotal());
    }

    @Test
    @DisplayName("测试根据单号查询入库单")
    public void testGetByNo() {
        log.info("开始测试根据单号查询入库单");

        ReceiptOrderCreateDTO dto = buildCreateDTO();
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        ReceiptOrder order = receiptOrderService.getById(orderId);

        ReceiptOrder found = receiptOrderService.getByNo(order.getReceiptNo());
        assertNotNull(found);
        assertEquals(orderId, found.getId());
        assertEquals(order.getReceiptNo(), found.getReceiptNo());
        log.info("根据单号查询入库单成功，单号: {}", found.getReceiptNo());
    }

    @Test
    @DisplayName("测试完整入库流程 - 从创建到完成")
    public void testCompleteReceiptFlow() {
        log.info("开始测试完整入库流程");

        String batchNo = "FULL-" + System.currentTimeMillis();
        String operator = "admin";

        log.info("步骤1: 创建入库单");
        ReceiptOrderCreateDTO dto = buildCreateDTOWithBatch(batchNo);
        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        ReceiptOrder order = receiptOrderService.getById(orderId);
        assertEquals(1, order.getOrderStatus());
        log.info("创建入库单成功: {}，状态: {}", order.getReceiptNo(), order.getOrderStatus());

        log.info("步骤2: 供应商到货确认");
        receiptOrderService.confirmArrival(orderId, new Date(), operator);
        order = receiptOrderService.getById(orderId);
        assertEquals(2, order.getOrderStatus());
        log.info("到货确认完成，状态: {}", order.getOrderStatus());

        log.info("步骤3: 入库质检");
        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(orderId);
        for (ReceiptOrderDetail detail : details) {
            receiptOrderService.doInspection(detail.getId(),
                    detail.getPlanQuantity(), detail.getPlanQuantity(),
                    BigDecimal.ZERO, 1, "全部合格", operator);
        }
        order = receiptOrderService.getById(orderId);
        assertEquals(4, order.getOrderStatus());
        log.info("质检完成，状态: {}", order.getOrderStatus());

        log.info("步骤4: 库位分配上架");
        details = receiptOrderService.getDetailsByOrderId(orderId);
        for (ReceiptOrderDetail detail : details) {
            receiptOrderService.assignLocation(detail.getId(), testLocationId, operator);
        }
        order = receiptOrderService.getById(orderId);
        assertEquals(5, order.getOrderStatus());
        log.info("库位分配完成，状态: {}", order.getOrderStatus());

        log.info("步骤5: 入库完成确认");
        receiptOrderService.confirmReceiptComplete(orderId, operator);
        order = receiptOrderService.getById(orderId);
        assertEquals(7, order.getOrderStatus());
        assertNotNull(order.getCompleteTime());
        log.info("入库完成，状态: {}, 完成时间: {}", order.getOrderStatus(), order.getCompleteTime());

        InventoryBatch batch = getBatchByNo(batchNo);
        assertNotNull(batch);
        assertEquals(0, new BigDecimal("100").compareTo(batch.getQuantity()));
        log.info("完整入库流程测试完成，库存已增加: {}", batch.getQuantity());
    }

    @Test
    @DisplayName("测试创建多明细入库单")
    public void testCreateMultiDetailReceiptOrder() {
        log.info("开始测试创建多明细入库单");

        ReceiptOrderCreateDTO dto = new ReceiptOrderCreateDTO();
        dto.setReceiptType(1);
        dto.setWarehouseId(testWarehouseId);
        dto.setSupplierId(testSupplierId);
        dto.setRemark("多明细入库单测试");

        List<ReceiptOrderCreateDTO.ReceiptDetailDTO> details = new ArrayList<>();

        ReceiptOrderCreateDTO.ReceiptDetailDTO detail1 = new ReceiptOrderCreateDTO.ReceiptDetailDTO();
        detail1.setProductId(1L);
        detail1.setBatchNo("BATCH-MULTI-1-" + System.currentTimeMillis());
        detail1.setPlanQuantity(new BigDecimal("50"));
        detail1.setUnit("台");
        details.add(detail1);

        ReceiptOrderCreateDTO.ReceiptDetailDTO detail2 = new ReceiptOrderCreateDTO.ReceiptDetailDTO();
        detail2.setProductId(2L);
        detail2.setBatchNo("BATCH-MULTI-2-" + System.currentTimeMillis());
        detail2.setPlanQuantity(new BigDecimal("30"));
        detail2.setUnit("件");
        details.add(detail2);

        dto.setDetails(details);

        Long orderId = receiptOrderService.createReceiptOrder(dto, operator);
        assertNotNull(orderId);

        List<ReceiptOrderDetail> createdDetails = receiptOrderService.getDetailsByOrderId(orderId);
        assertEquals(2, createdDetails.size());
        log.info("多明细入库单创建成功，明细数量: {}", createdDetails.size());

        BigDecimal totalQuantity = createdDetails.stream()
                .map(ReceiptOrderDetail::getPlanQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("80").compareTo(totalQuantity));
        log.info("多明细入库单总数量: {}", totalQuantity);
    }

    private ReceiptOrderCreateDTO buildCreateDTO() {
        return buildCreateDTOWithBatch("BATCH-" + System.currentTimeMillis());
    }

    private ReceiptOrderCreateDTO buildCreateDTOWithBatch(String batchNo) {
        ReceiptOrderCreateDTO dto = new ReceiptOrderCreateDTO();
        dto.setReceiptType(1);
        dto.setWarehouseId(testWarehouseId);
        dto.setSupplierId(testSupplierId);
        dto.setSourceOrderNo("PO-" + System.currentTimeMillis());
        dto.setRemark("测试入库单");

        List<ReceiptOrderCreateDTO.ReceiptDetailDTO> details = new ArrayList<>();
        ReceiptOrderCreateDTO.ReceiptDetailDTO detail = new ReceiptOrderCreateDTO.ReceiptDetailDTO();
        detail.setProductId(testProductId);
        detail.setBatchNo(batchNo);
        detail.setPlanQuantity(new BigDecimal("100"));
        detail.setUnit("台");
        Calendar cal = Calendar.getInstance();
        detail.setProduceDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 30);
        detail.setExpireDate(cal.getTime());
        detail.setCostPrice(new BigDecimal("99.99"));
        detail.setRemark("测试明细");
        details.add(detail);
        dto.setDetails(details);

        return dto;
    }

    private InventoryBatch getBatchByNo(String batchNo) {
        com.wms.dto.InventoryQueryDTO query = new com.wms.dto.InventoryQueryDTO();
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
