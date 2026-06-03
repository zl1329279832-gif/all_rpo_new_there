package com.wms.lock;

import com.wms.common.ResultCode;
import com.wms.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class RedisLock {

    private static final String LOCK_PREFIX = "wms:lock:";

    private static final long DEFAULT_WAIT_TIME = 3;
    private static final long DEFAULT_LEASE_TIME = 30;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    @Autowired
    private RedissonClient redissonClient;

    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, supplier);
    }

    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!locked) {
                log.warn("获取分布式锁失败: {}", lockKey);
                throw new BusinessException(ResultCode.LOCK_ACQUIRE_FAILED);
            }
            log.debug("获取分布式锁成功: {}", lockKey);
            return supplier.get();
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            throw new BusinessException(ResultCode.LOCK_ACQUIRE_FAILED);
        } catch (Exception e) {
            log.error("执行加锁业务异常: {}", lockKey, e);
            throw e;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    log.debug("释放分布式锁成功: {}", lockKey);
                } catch (Exception e) {
                    log.error("释放分布式锁异常: {}", lockKey, e);
                }
            }
        }
    }

    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, runnable);
    }

    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable runnable) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (!locked) {
                log.warn("获取分布式锁失败: {}", lockKey);
                throw new BusinessException(ResultCode.LOCK_ACQUIRE_FAILED);
            }
            log.debug("获取分布式锁成功: {}", lockKey);
            runnable.run();
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            throw new BusinessException(ResultCode.LOCK_ACQUIRE_FAILED);
        } catch (Exception e) {
            log.error("执行加锁业务异常: {}", lockKey, e);
            throw e;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    log.debug("释放分布式锁成功: {}", lockKey);
                } catch (Exception e) {
                    log.error("释放分布式锁异常: {}", lockKey, e);
                }
            }
        }
    }

    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT);
    }

    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
