package com.wms;

import com.wms.common.OutboundStrategy;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.InventoryQueryDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.InventoryLog;
import com.wms.entity.Location;
import com.wms.exception.BusinessException;
import com.wms.mapper.InventoryBatchMapper;
import com.wms.mapper.InventoryLogMapper;
import com.wms.mapper.LocationMapper;
import com.wms.service.InventoryService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@Rollback
@DisplayName("库存服务测试")
public class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Autowired
    private LocationMapper locationMapper;

    private Long testWarehouseId;
    private Long testLocationId;
    private Long testProductId;
    private Long testSupplierId;
    private String testBatchNo;

    @BeforeEach
    public void setUp() {
        testWarehouseId = 1L;
        testLocationId = 1L;
        testProductId = 1L;
        testSupplierId = 1L;
        testBatchNo = "BATCH-" + System.currentTimeMillis();
        log.info("初始化测试数据: warehouseId={}, locationId={}, productId={}, batchNo={}",
                testWarehouseId, testLocationId, testProductId, testBatchNo);
    }

    @Test
    @DisplayName("测试批次库存查询")
    public void testQueryInventoryBatch() {
        log.info("开始测试批次库存查询");

        stockInTestData(testBatchNo, new BigDecimal("100"));

        InventoryQueryDTO query = new InventoryQueryDTO();
        query.setWarehouseId(testWarehouseId);
        query.setProductId(testProductId);
        query.setBatchNo(testBatchNo);
        query.setPageNum(1);
        query.setPageSize(10);

        PageResult<InventoryBatch> result = inventoryService.queryInventoryBatch(query);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getList());
        assertFalse(result.getList().isEmpty());

        InventoryBatch batch = result.getList().get(0);
        assertEquals(testBatchNo, batch.getBatchNo());
        assertEquals(0, new BigDecimal("100").compareTo(batch.getQuantity()));
        log.info("批次库存查询测试完成，查询到 {} 条记录", result.getTotal());
    }

    @Test
    @DisplayName("测试库存入库 - 增加库存、记录流水")
    public void testStockIn() {
        log.info("开始测试库存入库");

        String batchNo = "INBOUND-" + System.currentTimeMillis();
        BigDecimal quantity = new BigDecimal("100");
        String unit = "台";
        Date produceDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date expireDate = cal.getTime();
        BigDecimal costPrice = new BigDecimal("99.99");
        String businessNo = "IN-" + System.currentTimeMillis();
        String operator = "testAdmin";
        String remark = "测试入库";

        inventoryService.stockIn(testWarehouseId, testLocationId, testProductId, batchNo,
                testSupplierId, quantity, unit, produceDate, expireDate,
                costPrice, businessNo, operator, remark);

        InventoryQueryDTO query = new InventoryQueryDTO();
        query.setWarehouseId(testWarehouseId);
        query.setProductId(testProductId);
        query.setBatchNo(batchNo);
        query.setPageNum(1);
        query.setPageSize(10);
        PageResult<InventoryBatch> batchResult = inventoryService.queryInventoryBatch(query);

        assertNotNull(batchResult);
        assertTrue(batchResult.getTotal() > 0);
        InventoryBatch batch = batchResult.getList().get(0);
        assertEquals(0, quantity.compareTo(batch.getQuantity()));
        assertEquals(0, quantity.compareTo(batch.getAvailableQuantity()));
        assertEquals(unit, batch.getUnit());
        assertEquals(testSupplierId, batch.getSupplierId());
        assertEquals(testLocationId, batch.getLocationId());
        log.info("库存入库成功，批次: {}, 数量: {}", batchNo, batch.getQuantity());

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);
        PageResult<InventoryLog> logResult = inventoryService.queryInventoryLog(
                pageQuery, testWarehouseId, testProductId, batchNo, 1, businessNo);

        assertNotNull(logResult);
        assertTrue(logResult.getTotal() > 0);
        InventoryLog inventoryLog = logResult.getList().get(0);
        assertEquals(0, quantity.compareTo(inventoryLog.getChangeQuantity()));
        assertEquals(1, inventoryLog.getOperationType());
        assertEquals(operator, inventoryLog.getOperator());
        assertEquals(businessNo, inventoryLog.getBusinessNo());
        log.info("库存流水记录成功，流水号: {}", inventoryLog.getLogNo());
    }

    @Test
    @DisplayName("测试FIFO出库策略分配 - 先进先出")
    public void testAllocateForOutboundFIFO() {
        log.info("开始测试FIFO出库策略分配");

        String batchNo1 = "FIFO-1-" + System.currentTimeMillis();
        String batchNo2 = "FIFO-2-" + (System.currentTimeMillis() + 1000);
        stockInTestData(batchNo1, new BigDecimal("50"));
        stockInTestData(batchNo2, new BigDecimal("50"));

        List<InventoryBatch> allocated = inventoryService.allocateForOutbound(
                testWarehouseId, testProductId, new BigDecimal("30"),
                OutboundStrategy.FIFO.getCode(), null);

        assertNotNull(allocated);
        assertFalse(allocated.isEmpty());
        assertTrue(allocated.size() >= 1);

        BigDecimal totalAllocated = allocated.stream()
                .map(InventoryBatch::getAvailableQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("30").compareTo(totalAllocated));
        assertEquals(batchNo1, allocated.get(0).getBatchNo(), "FIFO应该优先分配先入库的批次");
        log.info("FIFO出库策略分配测试完成，分配批次: {}, 分配数量: {}",
                allocated.get(0).getBatchNo(), totalAllocated);
    }

    @Test
    @DisplayName("测试FEFO出库策略分配 - 效期优先")
    public void testAllocateForOutboundFEFO() {
        log.info("开始测试FEFO出库策略分配");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 10);
        Date expireDate1 = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 20);
        Date expireDate2 = cal.getTime();

        String batchNo1 = "FEFO-1-" + System.currentTimeMillis();
        String batchNo2 = "FEFO-2-" + (System.currentTimeMillis() + 1000);

        stockInTestDataWithExpire(batchNo1, new BigDecimal("50"), expireDate1);
        stockInTestDataWithExpire(batchNo2, new BigDecimal("50"), expireDate2);

        List<InventoryBatch> allocated = inventoryService.allocateForOutbound(
                testWarehouseId, testProductId, new BigDecimal("30"),
                OutboundStrategy.FEFO.getCode(), null);

        assertNotNull(allocated);
        assertFalse(allocated.isEmpty());

        assertTrue(allocated.get(0).getExpireDate().before(allocated.get(0).getExpireDate()) ||
                allocated.get(0).getBatchNo().equals(batchNo1),
                "FEFO应该优先分配临近过期的批次");
        log.info("FEFO出库策略分配测试完成，优先分配批次: {}", allocated.get(0).getBatchNo());
    }

    @Test
    @DisplayName("测试库存锁定")
    public void testLockInventory() {
        log.info("开始测试库存锁定");

        String batchNo = "LOCK-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        assertNotNull(batch);
        Long batchId = batch.getId();
        String businessNo = "LOCK-" + System.currentTimeMillis();
        String operator = "testAdmin";

        inventoryService.lockInventory(batchId, new BigDecimal("30"), businessNo, operator);

        InventoryBatch afterLock = inventoryBatchMapper.selectById(batchId);
        assertNotNull(afterLock);
        assertEquals(0, new BigDecimal("70").compareTo(afterLock.getAvailableQuantity()));
        assertEquals(0, new BigDecimal("30").compareTo(afterLock.getLockedQuantity()));
        assertEquals(0, new BigDecimal("100").compareTo(afterLock.getQuantity()));
        log.info("库存锁定测试完成，可用: {}, 锁定: {}", afterLock.getAvailableQuantity(), afterLock.getLockedQuantity());
    }

    @Test
    @DisplayName("测试库存解锁")
    public void testUnlockInventory() {
        log.info("开始测试库存解锁");

        String batchNo = "UNLOCK-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        Long batchId = batch.getId();
        String businessNo = "UNLOCK-" + System.currentTimeMillis();
        String operator = "testAdmin";

        inventoryService.lockInventory(batchId, new BigDecimal("30"), businessNo, operator);
        inventoryService.unlockInventory(batchId, new BigDecimal("30"), businessNo, operator);

        InventoryBatch afterUnlock = inventoryBatchMapper.selectById(batchId);
        assertNotNull(afterUnlock);
        assertEquals(0, new BigDecimal("100").compareTo(afterUnlock.getAvailableQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(afterUnlock.getLockedQuantity()));
        log.info("库存解锁测试完成，可用: {}, 锁定: {}", afterUnlock.getAvailableQuantity(), afterUnlock.getLockedQuantity());
    }

    @Test
    @DisplayName("测试库存并发扣减 - 模拟多线程并发扣减")
    public void testConcurrentReduceInventory() throws InterruptedException {
        log.info("开始测试库存并发扣减");

        String batchNo = "CONCURRENT-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        final Long batchId = batch.getId();
        String operator = "testAdmin";

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String businessNo = "REDUCE-" + i + "-" + System.currentTimeMillis();
            executor.submit(() -> {
                try {
                    inventoryService.reduceInventory(batchId, new BigDecimal("10"), businessNo, operator);
                    successCount.incrementAndGet();
                    log.info("线程{}扣减成功", Thread.currentThread().getId());
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                    log.info("线程{}扣减失败: {}", Thread.currentThread().getId(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        InventoryBatch afterReduce = inventoryBatchMapper.selectById(batchId);
        assertNotNull(afterReduce);

        log.info("并发扣减测试完成: 成功数={}, 失败数={}", successCount.get(), failCount.get());
        log.info("剩余库存: {}, 预期剩余: {}", afterReduce.getQuantity(),
                new BigDecimal("100").subtract(new BigDecimal(successCount.get() * 10)));

        assertEquals(threadCount, successCount.get() + failCount.get());
        assertEquals(0, new BigDecimal("100").subtract(new BigDecimal(successCount.get() * 10)).compareTo(afterReduce.getQuantity()),
                "库存扣减后剩余数量应正确");
        assertTrue(afterReduce.getQuantity().compareTo(BigDecimal.ZERO) >= 0, "库存数量不能为负数");
    }

    @Test
    @DisplayName("测试库存冻结")
    public void testFreezeInventory() {
        log.info("开始测试库存冻结");

        String batchNo = "FREEZE-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        Long batchId = batch.getId();
        String businessNo = "FREEZE-" + System.currentTimeMillis();
        String operator = "testAdmin";

        inventoryService.freezeInventory(batchId, new BigDecimal("50"), businessNo, operator);

        InventoryBatch afterFreeze = inventoryBatchMapper.selectById(batchId);
        assertNotNull(afterFreeze);
        assertEquals(0, new BigDecimal("50").compareTo(afterFreeze.getAvailableQuantity()));
        assertEquals(0, new BigDecimal("50").compareTo(afterFreeze.getFrozenQuantity()));
        assertEquals(0, new BigDecimal("100").compareTo(afterFreeze.getQuantity()));
        log.info("库存冻结测试完成，可用: {}, 冻结: {}", afterFreeze.getAvailableQuantity(), afterFreeze.getFrozenQuantity());
    }

    @Test
    @DisplayName("测试库存解冻")
    public void testUnfreezeInventory() {
        log.info("开始测试库存解冻");

        String batchNo = "UNFREEZE-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        Long batchId = batch.getId();
        String businessNo = "UNFREEZE-" + System.currentTimeMillis();
        String operator = "testAdmin";

        inventoryService.freezeInventory(batchId, new BigDecimal("50"), businessNo, operator);
        inventoryService.unfreezeInventory(batchId, new BigDecimal("50"), businessNo, operator);

        InventoryBatch afterUnfreeze = inventoryBatchMapper.selectById(batchId);
        assertNotNull(afterUnfreeze);
        assertEquals(0, new BigDecimal("100").compareTo(afterUnfreeze.getAvailableQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(afterUnfreeze.getFrozenQuantity()));
        log.info("库存解冻测试完成，可用: {}, 冻结: {}", afterUnfreeze.getAvailableQuantity(), afterUnfreeze.getFrozenQuantity());
    }

    @Test
    @DisplayName("测试库位容量不足处理")
    public void testLocationCapacityNotEnough() {
        log.info("开始测试库位容量不足处理");

        Location location = locationMapper.selectById(testLocationId);
        if (location == null) {
            location = createTestLocation();
            testLocationId = location.getId();
        }

        BigDecimal maxCapacity = location.getMaxCapacity();
        BigDecimal overCapacity = maxCapacity.add(new BigDecimal("100"));
        log.info("库位最大容量: {}, 尝试入库: {}", maxCapacity, overCapacity);

        String batchNo = "CAPACITY-" + System.currentTimeMillis();
        String businessNo = "CAP-" + System.currentTimeMillis();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            inventoryService.stockIn(testWarehouseId, testLocationId, testProductId, batchNo,
                    testSupplierId, overCapacity, "台", new Date(), new Date(),
                    new BigDecimal("100"), businessNo, "testAdmin", "测试容量不足");
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("容量") || exception.getMessage().contains("不足"));
        log.info("库位容量不足测试完成，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试库存不足扣减失败")
    public void testInventoryShortageReduce() {
        log.info("开始测试库存不足扣减失败");

        String batchNo = "SHORTAGE-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("50"));

        InventoryBatch batch = getBatchByNo(batchNo);
        Long batchId = batch.getId();
        String businessNo = "SHORT-" + System.currentTimeMillis();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            inventoryService.reduceInventory(batchId, new BigDecimal("100"), businessNo, "testAdmin");
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("不足") || exception.getMessage().contains("库存"));
        log.info("库存不足扣减失败测试完成，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试库存流水查询")
    public void testQueryInventoryLog() {
        log.info("开始测试库存流水查询");

        String batchNo = "LOG-" + System.currentTimeMillis();
        String businessNo = "LOG-" + System.currentTimeMillis();
        stockInTestDataWithBusinessNo(batchNo, new BigDecimal("100"), businessNo);

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);

        PageResult<InventoryLog> result = inventoryService.queryInventoryLog(
                pageQuery, testWarehouseId, testProductId, batchNo, 1, businessNo);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getList());
        assertFalse(result.getList().isEmpty());

        InventoryLog inventoryLog = result.getList().get(0);
        assertEquals(businessNo, inventoryLog.getBusinessNo());
        assertEquals(batchNo, inventoryLog.getBatchNo());
        assertEquals(1, inventoryLog.getBusinessType());
        log.info("库存流水查询测试完成，查询到 {} 条流水记录", result.getTotal());
    }

    @Test
    @DisplayName("测试批次追溯查询")
    public void testQueryBatchTrace() {
        log.info("开始测试批次追溯查询");

        String batchNo = "TRACE-" + System.currentTimeMillis();
        String businessNo = "TRACE-" + System.currentTimeMillis();
        stockInTestDataWithBusinessNo(batchNo, new BigDecimal("100"), businessNo);

        InventoryBatch batch = getBatchByNo(batchNo);
        inventoryService.lockInventory(batch.getId(), new BigDecimal("20"), "LOCK-TRACE", "testAdmin");

        List<InventoryLog> traceLogs = inventoryService.queryBatchTrace(batchNo, testWarehouseId, testProductId);

        assertNotNull(traceLogs);
        assertTrue(traceLogs.size() >= 2);
        log.info("批次追溯查询测试完成，追溯到 {} 条操作记录", traceLogs.size());
    }

    @Test
    @DisplayName("测试库位容量检查")
    public void testCheckLocationCapacity() {
        log.info("开始测试库位容量检查");

        boolean result1 = inventoryService.checkLocationCapacity(testLocationId, new BigDecimal("10"));
        assertTrue(result1, "小数量入库应该容量充足");

        Location location = locationMapper.selectById(testLocationId);
        if (location != null && location.getMaxCapacity() != null) {
            BigDecimal overCapacity = location.getMaxCapacity().add(new BigDecimal("1"));
            boolean result2 = inventoryService.checkLocationCapacity(testLocationId, overCapacity);
            assertFalse(result2, "超出容量应该返回false");
        }

        log.info("库位容量检查测试完成");
    }

    @Test
    @DisplayName("测试根据ID查询批次")
    public void testGetById() {
        log.info("开始测试根据ID查询批次");

        String batchNo = "GETBYID-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        assertNotNull(batch);

        InventoryBatch found = inventoryService.getById(batch.getId());
        assertNotNull(found);
        assertEquals(batchNo, found.getBatchNo());
        log.info("根据ID查询批次测试完成，批次: {}", found.getBatchNo());
    }

    @Test
    @DisplayName("测试更新库存状态")
    public void testUpdateInventoryStatus() {
        log.info("开始测试更新库存状态");

        String batchNo = "STATUS-" + System.currentTimeMillis();
        stockInTestData(batchNo, new BigDecimal("100"));

        InventoryBatch batch = getBatchByNo(batchNo);
        Long batchId = batch.getId();

        inventoryService.updateInventoryStatus(batchId, 2, "testAdmin");

        InventoryBatch afterUpdate = inventoryBatchMapper.selectById(batchId);
        assertEquals(2, afterUpdate.getInventoryStatus());
        log.info("更新库存状态测试完成，状态: {}", afterUpdate.getInventoryStatus());
    }

    private void stockInTestData(String batchNo, BigDecimal quantity) {
        stockInTestDataWithBusinessNo(batchNo, quantity, "TEST-" + System.currentTimeMillis());
    }

    private void stockInTestDataWithBusinessNo(String batchNo, BigDecimal quantity, String businessNo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        inventoryService.stockIn(testWarehouseId, testLocationId, testProductId, batchNo,
                testSupplierId, quantity, "台", new Date(), cal.getTime(),
                new BigDecimal("100"), businessNo, "testAdmin", "测试数据");
    }

    private void stockInTestDataWithExpire(String batchNo, BigDecimal quantity, Date expireDate) {
        inventoryService.stockIn(testWarehouseId, testLocationId, testProductId, batchNo,
                testSupplierId, quantity, "台", new Date(), expireDate,
                new BigDecimal("100"), "TEST-" + System.currentTimeMillis(), "testAdmin", "测试数据");
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

    private Location createTestLocation() {
        Location location = new Location();
        location.setWarehouseId(testWarehouseId);
        location.setAreaId(1L);
        location.setLocationCode("TEST-LOC-" + System.currentTimeMillis());
        location.setLocationName("测试库位");
        location.setLocationType(1);
        location.setMaxCapacity(new BigDecimal("1000"));
        location.setCurrentQuantity(BigDecimal.ZERO);
        location.setAvailableCapacity(new BigDecimal("1000"));
        location.setStatus(1);
        location.setCreateBy("testAdmin");
        location.setCreateTime(new Date());
        location.setUpdateTime(new Date());
        locationMapper.insert(location);
        return location;
    }
}
