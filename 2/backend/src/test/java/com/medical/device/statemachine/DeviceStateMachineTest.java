package com.medical.device.statemachine;

import com.medical.device.enums.DeviceStatus;
import com.medical.device.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceStateMachineTest {

    private DeviceStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new DeviceStateMachine();
    }

    @Test
    void testNormalToMaintenance() {
        DeviceStatus result = stateMachine.transition(DeviceStatus.NORMAL, DeviceStatus.IN_MAINTENANCE, 1);
        assertEquals(DeviceStatus.IN_MAINTENANCE, result);
    }

    @Test
    void testNormalToRepair() {
        DeviceStatus result = stateMachine.transition(DeviceStatus.NORMAL, DeviceStatus.IN_REPAIR, 1);
        assertEquals(DeviceStatus.IN_REPAIR, result);
    }

    @Test
    void testRepairToNormal() {
        DeviceStatus result = stateMachine.transition(DeviceStatus.IN_REPAIR, DeviceStatus.NORMAL, 1);
        assertEquals(DeviceStatus.NORMAL, result);
    }

    @Test
    void testQcFailedCannotGoToNormal() {
        assertThrows(BusinessException.class, () -> {
            stateMachine.transition(DeviceStatus.IN_REPAIR, DeviceStatus.NORMAL, 2);
        });
    }

    @Test
    void testInvalidTransition() {
        assertThrows(BusinessException.class, () -> {
            stateMachine.transition(DeviceStatus.SCRAPPED, DeviceStatus.NORMAL, 1);
        });
    }

    @Test
    void testCanTransition() {
        assertTrue(stateMachine.canTransition(DeviceStatus.NORMAL, DeviceStatus.IN_REPAIR));
        assertFalse(stateMachine.canTransition(DeviceStatus.SCRAPPED, DeviceStatus.NORMAL));
    }

    @Test
    void testStartRepair() {
        assertEquals(DeviceStatus.IN_REPAIR, stateMachine.startRepair(DeviceStatus.NORMAL));
    }

    @Test
    void testCompleteRepair() {
        assertEquals(DeviceStatus.NORMAL, stateMachine.completeRepair(DeviceStatus.IN_REPAIR, 1));
    }

    @Test
    void testScrap() {
        assertEquals(DeviceStatus.SCRAPPED, stateMachine.scrap(DeviceStatus.NORMAL));
    }
}
