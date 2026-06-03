package com.wms;

import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
@DisplayName("Redis分布式锁测试")
public class RedisLockTest {

    @Autowired
    private RedisLock redisLock;

    private static final String TEST_LOCK_KEY = "test:lock:inventory:";

    @Test
    @DisplayName("测试获取锁并执行业务逻辑 - Supplier方式")
    public void testExecuteWithLockSupplier() {
        log.info("开始测试获取锁并执行业务逻辑 - Supplier方式");

        String lockKey = TEST_LOCK_KEY + "supplier:" + System.currentTimeMillis();

        String result = redisLock.executeWithLock(lockKey, () -> {
            log.info("执行业务逻辑，已获取锁: {}", lockKey);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "业务执行成功";
        });

        assertEquals("业务执行成功", result);
        log.info("Supplier方式锁测试完成，结果: {}", result);
    }

    @Test
    @DisplayName("测试获取锁并执行业务逻辑 - Runnable方式")
    public void testExecuteWithLockRunnable() {
        log.info("开始测试获取锁并执行业务逻辑 - Runnable方式");

        String lockKey = TEST_LOCK_KEY + "runnable:" + System.currentTimeMillis();
        final boolean[] executed = {false};

        redisLock.executeWithLock(lockKey, () -> {
            log.info("执行业务逻辑，已获取锁: {}", lockKey);
            executed[0] = true;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertTrue(executed[0]);
        log.info("Runnable方式锁测试完成");
    }

    @Test
    @DisplayName("测试可重入锁 - 同一线程重复获取锁")
    public void testReentrantLock() {
        log.info("开始测试可重入锁");

        String lockKey = TEST_LOCK_KEY + "reentrant:" + System.currentTimeMillis();
        final int[] executeCount = {0};

        String result = redisLock.executeWithLock(lockKey, () -> {
            executeCount[0]++;
            log.info("第一次获取锁成功，执行次数: {}", executeCount[0]);

            redisLock.executeWithLock(lockKey, () -> {
                executeCount[0]++;
                log.info("第二次获取锁成功(重入)，执行次数: {}", executeCount[0]);
                return "重入执行成功";
            });

            return "外层执行成功";
        });

        assertEquals("外层执行成功", result);
        assertEquals(2, executeCount[0], "应该执行了2次");
        log.info("可重入锁测试完成，执行次数: {}", executeCount[0]);
    }

    @Test
    @DisplayName("测试锁超时释放")
    public void testLockTimeoutRelease() throws InterruptedException {
        log.info("开始测试锁超时释放");

        String lockKey = TEST_LOCK_KEY + "timeout:" + System.currentTimeMillis();
        long leaseTime = 1;
        long waitTime = 3;

        Thread thread1 = new Thread(() -> {
            try {
                redisLock.executeWithLock(lockKey, waitTime, leaseTime, TimeUnit.SECONDS, () -> {
                    log.info("线程1获取锁成功，持有锁中...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    log.info("线程1业务执行完成");
                    return null;
                });
            } catch (Exception e) {
                log.info("线程1预期异常: {}", e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(1500);
                log.info("线程2尝试获取锁...");
                String result = redisLock.executeWithLock(lockKey, waitTime, leaseTime, TimeUnit.SECONDS, () -> {
                    log.info("线程2获取锁成功，说明锁已超时释放");
                    return "线程2获取锁成功";
                });
                assertEquals("线程2获取锁成功", result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        log.info("锁超时释放测试完成");
    }

    @Test
    @DisplayName("测试并发场景下的锁互斥 - 10个线程竞争")
    public void testConcurrentLockMutex() throws InterruptedException {
        log.info("开始测试并发场景下的锁互斥");

        String lockKey = TEST_LOCK_KEY + "concurrent:" + System.currentTimeMillis();
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Long> holdTimes = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    String result = redisLock.executeWithLock(lockKey, 2, 5, TimeUnit.SECONDS, () -> {
                        log.info("线程{}获取锁成功，开始执行业务", threadIndex);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        long holdTime = System.currentTimeMillis() - startTime;
                        holdTimes.add(holdTime);
                        log.info("线程{}业务执行完成，持有锁时间: {}ms", threadIndex, holdTime);
                        return "success";
                    });
                    if ("success".equals(result)) {
                        successCount.incrementAndGet();
                    }
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                    log.info("线程{}获取锁失败: {}", threadIndex, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        log.info("并发测试完成: 成功数={}, 失败数={}", successCount.get(), failCount.get());
        log.info("持有锁时间列表: {}", holdTimes);

        assertEquals(threadCount, successCount.get() + failCount.get(), "所有线程都应有结果");
        assertTrue(successCount.get() > 0, "至少有一个线程获取锁成功");
        log.info("并发锁互斥测试完成");
    }

    @Test
    @DisplayName("测试并发计数器 - 验证锁保证原子性")
    public void testConcurrentCounterWithLock() throws InterruptedException {
        log.info("开始测试并发计数器 - 验证锁保证原子性");

        String lockKey = TEST_LOCK_KEY + "counter:" + System.currentTimeMillis();
        int threadCount = 20;
        int incrementPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        final int[] counter = {0};

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementPerThread; j++) {
                        redisLock.executeWithLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
                            int current = counter[0];
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            counter[0] = current + 1;
                            return null;
                        });
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        int expectedTotal = threadCount * incrementPerThread;
        log.info("并发计数器测试完成: 预期值={}, 实际值={}", expectedTotal, counter[0]);

        assertEquals(expectedTotal, counter[0], "锁应该保证计数器的原子性，结果应等于预期值");
        log.info("并发计数器测试通过");
    }

    @Test
    @DisplayName("测试业务异常时锁自动释放")
    public void testLockReleaseOnException() {
        log.info("开始测试业务异常时锁自动释放");

        String lockKey = TEST_LOCK_KEY + "exception:" + System.currentTimeMillis();

        assertThrows(RuntimeException.class, () -> {
            redisLock.executeWithLock(lockKey, () -> {
                log.info("执行业务逻辑，将抛出异常");
                throw new RuntimeException("业务执行异常");
            });
        });

        String result = redisLock.executeWithLock(lockKey, () -> {
            log.info("异常后重新获取锁成功，说明锁已释放");
            return "重新获取成功";
        });

        assertEquals("重新获取成功", result);
        log.info("业务异常时锁自动释放测试完成");
    }

    @Test
    @DisplayName("测试手动获取和释放锁")
    public void testManualLockAndUnlock() {
        log.info("开始测试手动获取和释放锁");

        String lockKey = TEST_LOCK_KEY + "manual:" + System.currentTimeMillis();

        boolean locked = redisLock.tryLock(lockKey, 3, 10, TimeUnit.SECONDS);
        assertTrue(locked, "手动获取锁应该成功");
        log.info("手动获取锁成功: {}", lockKey);

        try {
            log.info("执行业务逻辑");
        } finally {
            redisLock.unlock(lockKey);
            log.info("手动释放锁成功");
        }

        boolean lockedAgain = redisLock.tryLock(lockKey, 3, 10, TimeUnit.SECONDS);
        assertTrue(lockedAgain, "释放后应该能重新获取锁");
        redisLock.unlock(lockKey);

        log.info("手动获取和释放锁测试完成");
    }

    @Test
    @DisplayName("测试获取锁失败抛出异常")
    public void testLockAcquireFailed() throws InterruptedException {
        log.info("开始测试获取锁失败抛出异常");

        String lockKey = TEST_LOCK_KEY + "fail:" + System.currentTimeMillis();

        Thread holdingThread = new Thread(() -> {
            redisLock.executeWithLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
                log.info("持有锁的线程开始执行，将持有5秒");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("持有锁的线程执行完成");
                return null;
            });
        });

        holdingThread.start();
        Thread.sleep(500);

        Thread competingThread = new Thread(() -> {
            try {
                redisLock.executeWithLock(lockKey, 1, 10, TimeUnit.SECONDS, () -> {
                    log.info("竞争线程获取锁成功，这不应该发生");
                    return null;
                });
                fail("竞争线程应该获取锁失败并抛出异常");
            } catch (BusinessException e) {
                log.info("竞争线程获取锁失败，抛出预期异常: {}", e.getMessage());
                assertNotNull(e.getMessage());
            }
        });

        competingThread.start();
        competingThread.join();
        holdingThread.join();

        log.info("获取锁失败抛出异常测试完成");
    }

    @Test
    @DisplayName("测试不同锁key互不影响")
    public void testDifferentLockKeysIndependent() throws InterruptedException {
        log.info("开始测试不同锁key互不影响");

        String lockKey1 = TEST_LOCK_KEY + "independent:1:" + System.currentTimeMillis();
        String lockKey2 = TEST_LOCK_KEY + "independent:2:" + System.currentTimeMillis();

        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        final boolean[] lock1Acquired = {false};
        final boolean[] lock2Acquired = {false};

        Thread thread1 = new Thread(() -> {
            redisLock.executeWithLock(lockKey1, 3, 10, TimeUnit.SECONDS, () -> {
                lock1Acquired[0] = true;
                latch1.countDown();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        });

        Thread thread2 = new Thread(() -> {
            try {
                latch1.await();
                redisLock.executeWithLock(lockKey2, 3, 10, TimeUnit.SECONDS, () -> {
                    lock2Acquired[0] = true;
                    latch2.countDown();
                    return null;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread1.start();
        thread2.start();

        latch2.await(5, TimeUnit.SECONDS);
        thread1.join();
        thread2.join();

        assertTrue(lock1Acquired[0], "锁1应该被获取");
        assertTrue(lock2Acquired[0], "锁2应该在锁1持有时也能被获取");
        log.info("不同锁key互不影响测试完成");
    }
}
