package com.medical.device.statemachine;

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
        assertDoesNotThrow(() -> stateMachine.transition(1, 2, 1));
    }

    @Test
    void testNormalToRepair() {
        assertDoesNotThrow(() -> stateMachine.transition(1, 3, 1));
    }

    @Test
    void testRepairToNormal() {
        assertDoesNotThrow(() -> stateMachine.transition(3, 1, 1));
    }

    @Test
    void testQcFailedCannotGoToNormal() {
        assertThrows(BusinessException.class, () -> {
            stateMachine.transition(3, 1, 2);
        });
    }

    @Test
    void testInvalidTransition() {
        assertThrows(BusinessException.class, () -> {
            stateMachine.transition(4, 1, 1);
        });
    }

    @Test
    void testCanTransition() {
        assertTrue(stateMachine.canTransition(1, 3));
        assertFalse(stateMachine.canTransition(4, 1));
    }
}
