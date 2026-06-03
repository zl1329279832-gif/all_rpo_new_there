package com.wms.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.single-server-config.address}")
    private String address;

    @Value("${redisson.single-server-config.password:}")
    private String password;

    @Value("${redisson.single-server-config.database}")
    private int database;

    @Value("${redisson.single-server-config.timeout}")
    private int timeout;

    @Value("${redisson.single-server-config.connection-pool-size}")
    private int connectionPoolSize;

    @Value("${redisson.single-server-config.connection-minimum-idle-size}")
    private int connectionMinimumIdleSize;

    @Value("${redisson.single-server-config.idle-connection-timeout}")
    private int idleConnectionTimeout;

    @Value("${redisson.single-server-config.connect-timeout}")
    private int connectTimeout;

    @Value("${redisson.single-server-config.retry-attempts}")
    private int retryAttempts;

    @Value("${redisson.single-server-config.retry-interval}")
    private int retryInterval;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setTimeout(timeout)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setConnectTimeout(connectTimeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval);

        if (password != null && !password.isEmpty()) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
