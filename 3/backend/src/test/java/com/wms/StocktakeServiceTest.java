package com.wms;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.InventoryQueryDTO;
import com.wms.dto.StocktakeResultDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.StocktakeOrder;
import com.wms.entity.StocktakeOrderDetail;
import com.wms.mapper.StocktakeOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.StocktakeService;
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
@DisplayName("盘点流程测试")
public class StocktakeServiceTest {

    @Autowired
    private StocktakeService stocktakeService;

    @Autowired
    private StocktakeOrderMapper stocktakeOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    private Long testWarehouseId;
    private Long testAreaId;
    private Long testLocationId;
    private Long testProductId;
    private String operator;

    @BeforeEach
    public void setUp() {
        testWarehouseId = 1L;
        testAreaId = 1L;
        testLocationId = 1L;
        testProductId = 1L;
        operator = "testAdmin";
        log.info("初始化盘点测试数据: warehouseId={}, locationId={}", testWarehouseId, testLocationId);
    }

    @Test
    @DisplayName("测试创建盘点单")
    public void testCreateStocktakeOrder() {
        log.info("开始测试创建盘点单");

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        assertNotNull(orderId, "创建盘点单应返回订单ID");
        log.info("创建盘点单成功，订单ID: {}", orderId);

        StocktakeOrder created = stocktakeService.getById(orderId);
        assertNotNull(created);
        assertEquals(1, created.getStatus(), "新建盘点单状态应为新建(1)");
        assertEquals(testWarehouseId, created.getWarehouseId());
        assertEquals(1, created.getStocktakeType());
        assertNotNull(created.getStocktakeNo());
        log.info("盘点单信息验证成功: 单号={}, 状态={}", created.getStocktakeNo(), created.getStatus());

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        assertNotNull(details);
        assertFalse(details.isEmpty());
        log.info("盘点单明细验证成功，明细数量: {}", details.size());
    }

    @Test
    @DisplayName("测试录入盘点结果")
    public void testEnterStocktakeResult() {
        log.info("开始测试录入盘点结果");

        String batchNo = "STOCKTAKE-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        assertFalse(details.isEmpty());

        StocktakeResultDTO dto = new StocktakeResultDTO();
        dto.setStocktakeId(orderId);
        dto.setCounter(operator);

        List<StocktakeResultDTO.StocktakeDetailDTO> resultDetails = new ArrayList<>();
        for (StocktakeOrderDetail detail : details) {
            StocktakeResultDTO.StocktakeDetailDTO resultDetail = new StocktakeResultDTO.StocktakeDetailDTO();
            resultDetail.setDetailId(detail.getId());
            resultDetail.setFirstCount(new BigDecimal("105"));
            resultDetail.setSecondCount(new BigDecimal("105"));
            resultDetail.setFinalCount(new BigDecimal("105"));
            resultDetail.setDiffReason("盘盈5个");
            resultDetails.add(resultDetail);
        }
        dto.setDetails(resultDetails);

        stocktakeService.enterStocktakeResult(dto, operator);

        StocktakeOrder updated = stocktakeService.getById(orderId);
        assertEquals(3, updated.getStatus(), "录入结果后状态应为盘点中(3)");
        assertNotNull(updated.getCountQuantity());
        log.info("录入盘点结果成功，状态: {}, 实盘数量: {}", updated.getStatus(), updated.getCountQuantity());

        for (StocktakeOrderDetail detail : stocktakeService.getDetailsByOrderId(orderId)) {
            assertEquals(1, detail.getIsCounted(), "明细应标记为已盘点");
            assertNotNull(detail.getFinalCount());
            assertEquals(0, new BigDecimal("105").compareTo(detail.getFinalCount()));
            assertEquals(1, detail.getDiffType(), "盘盈差异类型应为1");
            assertEquals(0, new BigDecimal("5").compareTo(detail.getDiffQuantity()), "盘盈数量应为5");
            log.info("明细盘点结果: 系统数量={}, 实盘数量={}, 差异数量={}, 差异类型={}",
                    detail.getSystemQuantity(), detail.getFinalCount(), detail.getDiffQuantity(), detail.getDiffType());
        }
    }

    @Test
    @DisplayName("测试盘盈处理 - 增加库存")
    public void testProcessDiffProfit() {
        log.info("开始测试盘盈处理");

        String batchNo = "PROFIT-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();

        StocktakeResultDTO resultDTO = buildResultDTO(orderId, detailId, 105, "盘盈5个");
        stocktakeService.enterStocktakeResult(resultDTO, operator);

        InventoryBatch beforeProcess = getBatchByNo(batchNo);
        BigDecimal beforeQuantity = beforeProcess.getQuantity();
        log.info("盘盈处理前库存: {}", beforeQuantity);

        stocktakeService.processDiff(detailId, 2, "盘盈入库，增加库存5", operator);

        StocktakeOrderDetail processedDetail = stocktakeOrderMapper.selectDetailById(detailId);
        assertEquals(2, processedDetail.getProcessStatus(), "处理状态应为已处理(2)");
        assertNotNull(processedDetail.getProcessResult());
        assertNotNull(processedDetail.getProcessor());
        log.info("盘盈处理完成，处理状态: {}", processedDetail.getProcessStatus());

        InventoryBatch afterProcess = getBatchByNo(batchNo);
        BigDecimal afterQuantity = afterProcess.getQuantity();
        log.info("盘盈处理后库存: {}", afterQuantity);

        assertEquals(0, beforeQuantity.add(new BigDecimal("5")).compareTo(afterQuantity),
                "盘盈处理后库存应增加5");
        log.info("盘盈处理测试完成，库存增加: {}", afterQuantity.subtract(beforeQuantity));
    }

    @Test
    @DisplayName("测试盘亏处理 - 减少库存")
    public void testProcessDiffLoss() {
        log.info("开始测试盘亏处理");

        String batchNo = "LOSS-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();

        StocktakeResultDTO resultDTO = buildResultDTO(orderId, detailId, 95, "盘亏5个");
        stocktakeService.enterStocktakeResult(resultDTO, operator);

        InventoryBatch beforeProcess = getBatchByNo(batchNo);
        BigDecimal beforeQuantity = beforeProcess.getQuantity();
        log.info("盘亏处理前库存: {}", beforeQuantity);

        stocktakeService.processDiff(detailId, 2, "盘亏出库，减少库存5", operator);

        StocktakeOrderDetail processedDetail = stocktakeOrderMapper.selectDetailById(detailId);
        assertEquals(2, processedDetail.getProcessStatus(), "处理状态应为已处理(2)");
        assertEquals(2, processedDetail.getDiffType(), "差异类型应为盘亏(2)");
        log.info("盘亏处理完成，差异类型: {}, 处理状态: {}",
                processedDetail.getDiffType(), processedDetail.getProcessStatus());

        InventoryBatch afterProcess = getBatchByNo(batchNo);
        BigDecimal afterQuantity = afterProcess.getQuantity();
        log.info("盘亏处理后库存: {}", afterQuantity);

        assertEquals(0, beforeQuantity.subtract(new BigDecimal("5")).compareTo(afterQuantity),
                "盘亏处理后库存应减少5");
        log.info("盘亏处理测试完成，库存减少: {}", beforeQuantity.subtract(afterQuantity));
    }

    @Test
    @DisplayName("测试盘点确认")
    public void testConfirmStocktakeComplete() {
        log.info("开始测试盘点确认");

        String batchNo = "CONFIRM-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();

        StocktakeResultDTO resultDTO = buildResultDTO(orderId, detailId, 100, "无差异");
        stocktakeService.enterStocktakeResult(resultDTO, operator);

        stocktakeService.processDiff(detailId, 2, "无差异，无需处理", operator);

        stocktakeService.confirmStocktakeComplete(orderId, operator);

        StocktakeOrder completed = stocktakeService.getById(orderId);
        assertEquals(5, completed.getStatus(), "盘点确认后状态应为已完成(5)");
        assertNotNull(completed.getFinishTime());
        assertNotNull(completed.getCountQuantity());
        log.info("盘点确认成功，状态: {}, 完成时间: {}", completed.getStatus(), completed.getFinishTime());

        assertEquals(0, new BigDecimal("100").compareTo(completed.getCountQuantity()),
                "实盘总数量应为100");
        assertEquals(0, BigDecimal.ZERO.compareTo(completed.getProfitQuantity()),
                "盘盈总数量应为0");
        assertEquals(0, BigDecimal.ZERO.compareTo(completed.getLossQuantity()),
                "盘亏总数量应为0");
        log.info("盘点统计: 实盘={}, 盘盈={}, 盘亏={}",
                completed.getCountQuantity(), completed.getProfitQuantity(), completed.getLossQuantity());
    }

    @Test
    @DisplayName("测试完整盘点流程 - 盘盈场景")
    public void testCompleteStocktakeFlowProfit() {
        log.info("开始测试完整盘点流程 - 盘盈场景");

        String batchNo = "FULL-PROFIT-" + System.currentTimeMillis();
        String operator = "admin";
        prepareStockWithBatch(batchNo, 100);

        log.info("步骤1: 创建盘点单");
        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);
        StocktakeOrder created = stocktakeService.getById(orderId);
        assertEquals(1, created.getStatus());
        log.info("创建盘点单成功: {}，状态: {}", created.getStocktakeNo(), created.getStatus());

        log.info("步骤2: 录入盘点结果 - 盘盈5个");
        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();
        StocktakeResultDTO resultDTO = buildResultDTO(orderId, detailId, 105, "盘盈5个，记账错误");
        stocktakeService.enterStocktakeResult(resultDTO, operator);

        StocktakeOrder afterEnter = stocktakeService.getById(orderId);
        assertEquals(3, afterEnter.getStatus());
        log.info("录入盘点结果完成，状态: {}", afterEnter.getStatus());

        log.info("步骤3: 处理盘盈差异");
        InventoryBatch beforeProcess = getBatchByNo(batchNo);
        log.info("处理前库存: {}", beforeProcess.getQuantity());

        stocktakeService.processDiff(detailId, 2, "盘盈入库，调整库存", operator);

        InventoryBatch afterProcess = getBatchByNo(batchNo);
        assertEquals(0, new BigDecimal("105").compareTo(afterProcess.getQuantity()));
        log.info("处理后库存: {}", afterProcess.getQuantity());

        log.info("步骤4: 盘点确认完成");
        stocktakeService.confirmStocktakeComplete(orderId, operator);

        StocktakeOrder completed = stocktakeService.getById(orderId);
        assertEquals(5, completed.getStatus());
        assertNotNull(completed.getFinishTime());
        log.info("盘点确认完成，状态: {}", completed.getStatus());

        assertEquals(0, new BigDecimal("5").compareTo(completed.getProfitQuantity()),
                "盘盈总数量应为5");
        log.info("完整盘点流程(盘盈)测试完成，盘盈: {}", completed.getProfitQuantity());
    }

    @Test
    @DisplayName("测试完整盘点流程 - 盘亏场景")
    public void testCompleteStocktakeFlowLoss() {
        log.info("开始测试完整盘点流程 - 盘亏场景");

        String batchNo = "FULL-LOSS-" + System.currentTimeMillis();
        String operator = "admin";
        prepareStockWithBatch(batchNo, 100);

        log.info("步骤1: 创建盘点单");
        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);
        log.info("创建盘点单成功，ID: {}", orderId);

        log.info("步骤2: 录入盘点结果 - 盘亏5个");
        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();
        StocktakeResultDTO resultDTO = buildResultDTO(orderId, detailId, 95, "盘亏5个，丢失");
        stocktakeService.enterStocktakeResult(resultDTO, operator);
        log.info("录入盘点结果完成");

        log.info("步骤3: 处理盘亏差异");
        stocktakeService.processDiff(detailId, 2, "盘亏出库，报损处理", operator);

        InventoryBatch afterProcess = getBatchByNo(batchNo);
        assertEquals(0, new BigDecimal("95").compareTo(afterProcess.getQuantity()));
        log.info("处理后库存: {}", afterProcess.getQuantity());

        log.info("步骤4: 盘点确认完成");
        stocktakeService.confirmStocktakeComplete(orderId, operator);

        StocktakeOrder completed = stocktakeService.getById(orderId);
        assertEquals(5, completed.getStatus());
        assertEquals(0, new BigDecimal("5").compareTo(completed.getLossQuantity()),
                "盘亏总数量应为5");
        log.info("完整盘点流程(盘亏)测试完成，盘亏: {}", completed.getLossQuantity());
    }

    @Test
    @DisplayName("测试盘点单查询")
    public void testQueryStocktakeOrders() {
        log.info("开始测试盘点单查询");

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        PageResult<StocktakeOrder> result = stocktakeService.queryStocktakeOrders(
                pageQuery, 1, 1, testWarehouseId, null, null);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getList());
        assertFalse(result.getList().isEmpty());

        boolean found = result.getList().stream()
                .anyMatch(o -> o.getId().equals(orderId));
        assertTrue(found, "查询结果应包含刚创建的盘点单");

        log.info("盘点单查询测试完成，查询到 {} 条记录", result.getTotal());
    }

    @Test
    @DisplayName("测试根据单号查询盘点单")
    public void testGetByNo() {
        log.info("开始测试根据单号查询盘点单");

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);
        StocktakeOrder created = stocktakeService.getById(orderId);

        StocktakeOrder found = stocktakeService.getByNo(created.getStocktakeNo());
        assertNotNull(found);
        assertEquals(orderId, found.getId());
        assertEquals(created.getStocktakeNo(), found.getStocktakeNo());
        log.info("根据单号查询盘点单成功，单号: {}", found.getStocktakeNo());
    }

    @Test
    @DisplayName("测试无差异盘点流程")
    public void testNoDiffStocktakeFlow() {
        log.info("开始测试无差异盘点流程");

        String batchNo = "NODIFF-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo, 100);

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        Long detailId = details.get(0).getId();

        StocktakeResultDTO resultDTO = buildResultDTO(orderId, detailId, 100, "账实相符");
        stocktakeService.enterStocktakeResult(resultDTO, operator);

        StocktakeOrderDetail detail = stocktakeOrderMapper.selectDetailById(detailId);
        assertEquals(3, detail.getDiffType(), "无差异类型应为3");
        assertEquals(0, BigDecimal.ZERO.compareTo(detail.getDiffQuantity()), "差异数量应为0");
        log.info("无差异盘点: 系统数量={}, 实盘数量={}, 差异类型={}",
                detail.getSystemQuantity(), detail.getFinalCount(), detail.getDiffType());

        stocktakeService.processDiff(detailId, 2, "无差异，无需调整", operator);
        stocktakeService.confirmStocktakeComplete(orderId, operator);

        StocktakeOrder completed = stocktakeService.getById(orderId);
        assertEquals(5, completed.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(completed.getProfitQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(completed.getLossQuantity()));
        log.info("无差异盘点流程测试完成");
    }

    @Test
    @DisplayName("测试多明细盘点单")
    public void testMultiDetailStocktake() {
        log.info("开始测试多明细盘点单");

        String batchNo1 = "MULTI-1-" + System.currentTimeMillis();
        String batchNo2 = "MULTI-2-" + System.currentTimeMillis();
        prepareStockWithBatch(batchNo1, 100);
        prepareStockWithBatchAndProduct(batchNo2, 50, 2L);

        StocktakeOrder order = buildStocktakeOrder();
        Long orderId = stocktakeService.createStocktakeOrder(order, operator);

        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(orderId);
        assertTrue(details.size() >= 2);
        log.info("多明细盘点单创建成功，明细数量: {}", details.size());

        StocktakeResultDTO dto = new StocktakeResultDTO();
        dto.setStocktakeId(orderId);
        dto.setCounter(operator);

        List<StocktakeResultDTO.StocktakeDetailDTO> resultDetails = new ArrayList<>();
        int count = 0;
        for (StocktakeOrderDetail detail : details) {
            StocktakeResultDTO.StocktakeDetailDTO resultDetail = new StocktakeResultDTO.StocktakeDetailDTO();
            resultDetail.setDetailId(detail.getId());
            BigDecimal finalCount = detail.getSystemQuantity().add(new BigDecimal(count % 2 == 0 ? 5 : -3));
            resultDetail.setFirstCount(finalCount);
            resultDetail.setFinalCount(finalCount);
            resultDetail.setDiffReason(count % 2 == 0 ? "盘盈" : "盘亏");
            resultDetails.add(resultDetail);
            count++;
        }
        dto.setDetails(resultDetails);

        stocktakeService.enterStocktakeResult(dto, operator);

        StocktakeOrder afterEnter = stocktakeService.getById(orderId);
        assertNotNull(afterEnter.getCountQuantity());
        log.info("多明细盘点录入完成，实盘总数量: {}", afterEnter.getCountQuantity());
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

    private StocktakeOrder buildStocktakeOrder() {
        StocktakeOrder order = new StocktakeOrder();
        order.setStocktakeType(1);
        order.setWarehouseId(testWarehouseId);
        order.setAreaId(testAreaId);
        order.setStocktakeMethod(1);
        order.setHandler(operator);
        order.setRemark("测试盘点单");
        order.setCreateBy(operator);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        return order;
    }

    private StocktakeResultDTO buildResultDTO(Long orderId, Long detailId, int finalCount, String reason) {
        StocktakeResultDTO dto = new StocktakeResultDTO();
        dto.setStocktakeId(orderId);
        dto.setCounter(operator);

        List<StocktakeResultDTO.StocktakeDetailDTO> details = new ArrayList<>();
        StocktakeResultDTO.StocktakeDetailDTO detail = new StocktakeResultDTO.StocktakeDetailDTO();
        detail.setDetailId(detailId);
        detail.setFirstCount(new BigDecimal(finalCount));
        detail.setSecondCount(new BigDecimal(finalCount));
        detail.setFinalCount(new BigDecimal(finalCount));
        detail.setDiffReason(reason);
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
