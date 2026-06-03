package com.wms;

import com.wms.exception.BusinessException;
import com.wms.statemachine.InventoryEvent;
import com.wms.statemachine.InventoryState;
import com.wms.statemachine.InventoryStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@Rollback
@DisplayName("库存状态机测试")
public class InventoryStateMachineTest {

    @Autowired
    private InventoryStateMachine stateMachine;

    @Test
    @DisplayName("测试正常状态流转: NORMAL -> LOCKED -> FROZEN -> NORMAL")
    public void testNormalStateTransition() {
        log.info("开始测试状态流转: NORMAL -> LOCKED -> FROZEN -> NORMAL");

        InventoryState currentState = InventoryState.NORMAL;
        log.info("初始状态: {}", currentState.getName());

        currentState = stateMachine.transition(currentState, InventoryEvent.LOCK);
        assertEquals(InventoryState.LOCKED, currentState, "锁定后状态应为LOCKED");
        log.info("锁定后状态: {}", currentState.getName());

        currentState = stateMachine.transition(currentState, InventoryEvent.FREEZE);
        assertEquals(InventoryState.FROZEN, currentState, "冻结后状态应为FROZEN");
        log.info("冻结后状态: {}", currentState.getName());

        currentState = stateMachine.transition(currentState, InventoryEvent.UNFREEZE);
        assertEquals(InventoryState.NORMAL, currentState, "解冻后状态应为NORMAL");
        log.info("解冻后状态: {}", currentState.getName());

        log.info("状态流转测试完成");
    }

    @Test
    @DisplayName("测试解锁状态流转: LOCKED -> NORMAL")
    public void testUnlockTransition() {
        log.info("开始测试解锁状态流转");

        InventoryState currentState = InventoryState.LOCKED;
        InventoryState targetState = stateMachine.transition(currentState, InventoryEvent.UNLOCK);

        assertEquals(InventoryState.NORMAL, targetState, "解锁后状态应为NORMAL");
        log.info("解锁状态流转测试完成: {} -> {}", currentState.getName(), targetState.getName());
    }

    @Test
    @DisplayName("测试临期状态判断")
    public void testNearExpireState() {
        log.info("开始测试临期状态判断");

        int warningDays = 7;

        InventoryState state1 = stateMachine.getExpireState(10, warningDays);
        assertEquals(InventoryState.NORMAL, state1, "剩余10天(>7天)应为正常状态");
        log.info("剩余10天, 预警7天: 状态={}", state1.getName());

        InventoryState state2 = stateMachine.getExpireState(5, warningDays);
        assertEquals(InventoryState.NEAR_EXPIRE, state2, "剩余5天(<=7天)应为临期状态");
        log.info("剩余5天, 预警7天: 状态={}", state2.getName());

        boolean isNearExpire = stateMachine.checkExpire(InventoryState.NORMAL, 5, warningDays);
        assertTrue(isNearExpire, "剩余5天应判断为临期");
        log.info("临期状态判断测试完成");
    }

    @Test
    @DisplayName("测试过期状态判断")
    public void testExpiredState() {
        log.info("开始测试过期状态判断");

        int warningDays = 7;

        InventoryState state1 = stateMachine.getExpireState(0, warningDays);
        assertEquals(InventoryState.EXPIRED, state1, "剩余0天应为过期状态");
        log.info("剩余0天: 状态={}", state1.getName());

        InventoryState state2 = stateMachine.getExpireState(-1, warningDays);
        assertEquals(InventoryState.EXPIRED, state2, "剩余-1天应为过期状态");
        log.info("剩余-1天: 状态={}", state2.getName());

        boolean isExpired = stateMachine.checkExpire(InventoryState.NORMAL, 0, warningDays);
        assertTrue(isExpired, "剩余0天应判断为过期");
        log.info("过期状态判断测试完成");
    }

    @Test
    @DisplayName("测试已过期状态保持")
    public void testExpiredStateRemain() {
        log.info("开始测试已过期状态保持");

        boolean isExpired = stateMachine.checkExpire(InventoryState.EXPIRED, 100, 7);
        assertTrue(isExpired, "已过期状态即使剩余天数多也应返回过期");
        log.info("已过期状态保持测试完成");
    }

    @Test
    @DisplayName("测试非法状态转换抛出异常 - 从FROZEN直接LOCK")
    public void testIllegalTransitionFromFrozenToLock() {
        log.info("开始测试非法状态转换: FROZEN -> LOCK");

        InventoryState currentState = InventoryState.FROZEN;

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            stateMachine.transition(currentState, InventoryEvent.LOCK);
        }, "冻结状态不允许锁定操作，应抛出异常");

        log.info("捕获到预期异常: {}", exception.getMessage());
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("不允许"));
        log.info("非法状态转换测试完成");
    }

    @Test
    @DisplayName("测试非法状态转换抛出异常 - 从EXPIRED直接UNLOCK")
    public void testIllegalTransitionFromExpiredToUnlock() {
        log.info("开始测试非法状态转换: EXPIRED -> UNLOCK");

        InventoryState currentState = InventoryState.EXPIRED;

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            stateMachine.transition(currentState, InventoryEvent.UNLOCK);
        }, "过期状态不允许解锁操作，应抛出异常");

        log.info("捕获到预期异常: {}", exception.getMessage());
        assertNotNull(exception.getMessage());
        log.info("非法状态转换测试完成");
    }

    @Test
    @DisplayName("测试空状态转换抛出异常")
    public void testNullStateTransition() {
        log.info("开始测试空状态转换");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            stateMachine.transition(null, InventoryEvent.LOCK);
        }, "空状态应抛出异常");

        log.info("捕获到预期异常: {}", exception.getMessage());
        assertNotNull(exception.getMessage());
        log.info("空状态转换测试完成");
    }

    @Test
    @DisplayName("测试入库出库不改变状态")
    public void testInboundOutboundNotChangeState() {
        log.info("开始测试入库出库不改变状态");

        InventoryState normalState = InventoryState.NORMAL;
        InventoryState afterInbound = stateMachine.transition(normalState, InventoryEvent.INBOUND);
        assertEquals(InventoryState.NORMAL, afterInbound, "入库不改变状态");
        log.info("入库后状态: {}", afterInbound.getName());

        InventoryState afterOutbound = stateMachine.transition(normalState, InventoryEvent.OUTBOUND);
        assertEquals(InventoryState.NORMAL, afterOutbound, "出库不改变状态");
        log.info("出库后状态: {}", afterOutbound.getName());

        log.info("入库出库不改变状态测试完成");
    }

    @Test
    @DisplayName("测试临期状态可以锁定和冻结")
    public void testNearExpireAllowedTransitions() {
        log.info("开始测试临期状态允许的转换");

        InventoryState nearExpireState = InventoryState.NEAR_EXPIRE;

        assertTrue(nearExpireState.canTransition(InventoryEvent.LOCK), "临期状态允许锁定");
        assertTrue(nearExpireState.canTransition(InventoryEvent.FREEZE), "临期状态允许冻结");
        assertTrue(nearExpireState.canTransition(InventoryEvent.EXPIRE), "临期状态允许过期");
        assertTrue(nearExpireState.canTransition(InventoryEvent.OUTBOUND), "临期状态允许出库");

        log.info("临期状态可以锁定和冻结测试完成");
    }

    @Test
    @DisplayName("测试状态码转换")
    public void testStateFromCode() {
        log.info("开始测试状态码转换");

        assertEquals(InventoryState.NORMAL, InventoryState.fromCode(1));
        assertEquals(InventoryState.LOCKED, InventoryState.fromCode(2));
        assertEquals(InventoryState.FROZEN, InventoryState.fromCode(3));
        assertEquals(InventoryState.EXPIRED, InventoryState.fromCode(4));
        assertEquals(InventoryState.NEAR_EXPIRE, InventoryState.fromCode(5));
        assertNull(InventoryState.fromCode(999));
        assertNull(InventoryState.fromCode(null));

        log.info("状态码转换测试完成");
    }
}
