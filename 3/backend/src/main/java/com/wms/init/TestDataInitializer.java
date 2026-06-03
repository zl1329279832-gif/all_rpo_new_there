package com.wms.init;

import com.wms.entity.*;
import com.wms.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

@Component
@Order(1)
public class TestDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestDataInitializer.class);

    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;
    @Autowired
    private InventoryAlertMapper inventoryAlertMapper;

    @Override
    public void run(String... args) {
        logger.info("========== 开始初始化测试数据 ==========");
        try {
            initWarehouse();
            initLocation();
            initSupplier();
            initProduct();
            initUser();
            initInventoryBatch();
            initInventoryAlert();
            logger.info("========== 测试数据初始化完成 ==========");
        } catch (Exception e) {
            logger.error("测试数据初始化失败", e);
        }
    }

    private void initWarehouse() {
        if (warehouseMapper.selectCount() > 0) {
            logger.info("仓库数据已存在，跳过初始化");
            return;
        }
        String[] names = {"中心仓库", "华东分仓", "华南分仓"};
        String[] codes = {"WH001", "WH002", "WH003"};
        String[] addresses = {"上海市浦东新区", "杭州市余杭区", "广州市天河区"};
        for (int i = 0; i < names.length; i++) {
            Warehouse w = new Warehouse();
            w.setWarehouseCode(codes[i]);
            w.setWarehouseName(names[i]);
            w.setAddress(addresses[i]);
            w.setManager("管理员" + (i + 1));
            w.setPhone("1380000000" + (i + 1));
            w.setStatus(1);
            w.setRemark("测试仓库" + (i + 1));
            warehouseMapper.insert(w);
        }
        logger.info("仓库数据初始化完成，共{}条", names.length);
    }

    private void initLocation() {
        if (locationMapper.selectCount() > 0) {
            logger.info("库位数据已存在，跳过初始化");
            return;
        }
        String[] zones = {"A区", "B区", "C区", "D区"};
        String[] types = {"常温", "冷藏", "恒温", "贵重品"};
        int count = 0;
        for (int z = 0; z < zones.length; z++) {
            for (int row = 1; row <= 5; row++) {
                for (int col = 1; col <= 10; col++) {
                    Location loc = new Location();
                    loc.setWarehouseId(1L);
                    loc.setZoneCode(zones[z].substring(0, 1));
                    loc.setZoneName(zones[z]);
                    loc.setLocationCode(String.format("%s-%02d-%02d", zones[z].substring(0, 1), row, col));
                    loc.setLocationName(String.format("%s第%d排第%d列", zones[z], row, col));
                    loc.setLocationType(z + 1);
                    loc.setRowNum(row);
                    loc.setColNum(col);
                    loc.setLayerNum(1);
                    loc.setMaxCapacity(new BigDecimal("1000"));
                    loc.setUsedCapacity(new BigDecimal("0"));
                    loc.setStatus(1);
                    locationMapper.insert(loc);
                    count++;
                }
            }
        }
        logger.info("库位数据初始化完成，共{}条", count);
    }

    private void initSupplier() {
        if (supplierMapper.selectCount() > 0) {
            logger.info("供应商数据已存在，跳过初始化");
            return;
        }
        String[] names = {"华为技术有限公司", "小米科技有限公司", "苹果电子产品商贸", "三星电子", "联想集团", "海尔集团"};
        String[] codes = {"SUP001", "SUP002", "SUP003", "SUP004", "SUP005", "SUP006"};
        for (int i = 0; i < names.length; i++) {
            Supplier s = new Supplier();
            s.setSupplierCode(codes[i]);
            s.setSupplierName(names[i]);
            s.setContact("联系人" + (i + 1));
            s.setPhone("1390000000" + (i + 1));
            s.setEmail("supplier" + (i + 1) + "@example.com");
            s.setAddress("供应商地址" + (i + 1));
            s.setStatus(1);
            supplierMapper.insert(s);
        }
        logger.info("供应商数据初始化完成，共{}条", names.length);
    }

    private void initProduct() {
        if (productMapper.selectCount() > 0) {
            logger.info("商品数据已存在，跳过初始化");
            return;
        }
        String[] names = {"华为Mate 60 Pro", "小米14", "iPhone 15 Pro", "三星Galaxy S24", "ThinkPad X1 Carbon", "海尔冰箱BCD-500"};
        String[] codes = {"SKU001", "SKU002", "SKU003", "SKU004", "SKU005", "SKU006"};
        String[] specs = {"12GB+512GB", "8GB+256GB", "256GB", "256GB", "16GB+1TB", "500升"};
        String[] units = {"台", "台", "台", "台", "台", "台"};
        BigDecimal[] prices = {new BigDecimal("6999"), new BigDecimal("3999"), new BigDecimal("7999"),
                new BigDecimal("5999"), new BigDecimal("12999"), new BigDecimal("4999")};
        Integer[] warningDays = {30, 30, 30, 30, 60, 90};
        BigDecimal[] minStocks = {new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"),
                new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("5")};
        BigDecimal[] maxStocks = {new BigDecimal("500"), new BigDecimal("500"), new BigDecimal("500"),
                new BigDecimal("500"), new BigDecimal("200"), new BigDecimal("100")};
        for (int i = 0; i < names.length; i++) {
            Product p = new Product();
            p.setProductCode(codes[i]);
            p.setProductName(names[i]);
            p.setProductSpec(specs[i]);
            p.setUnit(units[i]);
            p.setPrice(prices[i]);
            p.setCategoryId((long) ((i / 3) + 1));
            p.setCategoryName(i < 3 ? "手机" : i < 5 ? "电脑" : "家电");
            p.setSupplierId((long) (i + 1));
            p.setWarningDays(warningDays[i]);
            p.setMinStock(minStocks[i]);
            p.setMaxStock(maxStocks[i]);
            p.setOutboundStrategy(i % 2 == 0 ? 1 : 2);
            p.setStatus(1);
            productMapper.insert(p);
        }
        logger.info("商品数据初始化完成，共{}条", names.length);
    }

    private void initUser() {
        if (userMapper.selectCount() > 0) {
            logger.info("用户数据已存在，跳过初始化");
            return;
        }
        String[] usernames = {"admin", "warehouse", "inspector", "picker", "manager"};
        String[] realNames = {"系统管理员", "仓库管理员", "质检员", "拣货员", "经理"};
        String[] roles = {"管理员", "仓管", "质检", "拣货", "经理"};
        for (int i = 0; i < usernames.length; i++) {
            User u = new User();
            u.setUsername(usernames[i]);
            u.setPassword("e10adc3949ba59abbe56e057f20f883e");
            u.setRealName(realNames[i]);
            u.setRole(roles[i]);
            u.setPhone("1360000000" + (i + 1));
            u.setEmail(usernames[i] + "@wms.com");
            u.setWarehouseId(1L);
            u.setStatus(1);
            userMapper.insert(u);
        }
        logger.info("用户数据初始化完成，共{}条", usernames.length);
    }

    private void initInventoryBatch() {
        if (inventoryBatchMapper.selectCount() > 0) {
            logger.info("批次库存数据已存在，跳过初始化");
            return;
        }
        Long[] locationIds = {1L, 2L, 3L, 4L, 5L, 6L};
        Date now = new Date();
        long dayMs = 24L * 60 * 60 * 1000;
        for (int i = 0; i < 6; i++) {
            InventoryBatch batch = new InventoryBatch();
            batch.setWarehouseId(1L);
            batch.setProductId((long) (i + 1));
            batch.setBatchNo(String.format("BATCH%06d", (i + 1)));
            batch.setLocationId(locationIds[i]);
            batch.setQuantity(new BigDecimal("100").subtract(new BigDecimal(i * 10)));
            batch.setAvailableQuantity(new BigDecimal("100").subtract(new BigDecimal(i * 10)));
            batch.setLockedQuantity(new BigDecimal("0"));
            batch.setFrozenQuantity(new BigDecimal("0"));
            batch.setInboundDate(now);
            batch.setExpireDate(new Date(now.getTime() + (180L - i * 20) * dayMs));
            batch.setInboundNo("IN20250100" + (i + 1));
            batch.setSupplierId((long) (i + 1));
            batch.setInventoryStatus(1);
            batch.setRemark("初始化测试批次");
            inventoryBatchMapper.insert(batch);
        }
        logger.info("批次库存数据初始化完成，共6条");
    }

    private void initInventoryAlert() {
        if (inventoryAlertMapper.selectCount() > 0) {
            logger.info("库存预警数据已存在，跳过初始化");
            return;
        }
        InventoryAlert alert1 = new InventoryAlert();
        alert1.setWarehouseId(1L);
        alert1.setProductId(1L);
        alert1.setBatchNo("BATCH000001");
        alert1.setAlertType(3);
        alert1.setAlertLevel(2);
        alert1.setAlertMessage("库存低于安全库存，当前库存：90，安全库存：100");
        alert1.setCurrentValue(new BigDecimal("90"));
        alert1.setThresholdValue(new BigDecimal("100"));
        alert1.setStatus(0);
        inventoryAlertMapper.insert(alert1);

        InventoryAlert alert2 = new InventoryAlert();
        alert2.setWarehouseId(1L);
        alert2.setProductId(6L);
        alert2.setBatchNo("BATCH000006");
        alert2.setAlertType(2);
        alert2.setAlertLevel(2);
        alert2.setAlertMessage("商品即将临期，距过期还有60天");
        alert2.setCurrentValue(new BigDecimal("60"));
        alert2.setThresholdValue(new BigDecimal("90"));
        alert2.setStatus(0);
        inventoryAlertMapper.insert(alert2);

        logger.info("库存预警数据初始化完成，共2条");
    }
}
