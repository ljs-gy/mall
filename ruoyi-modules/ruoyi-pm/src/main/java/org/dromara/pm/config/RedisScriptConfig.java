package org.dromara.pm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 下单资产扣减 Lua（无 per-SKU 占痕；取消靠单号防重加回）
 */
@Configuration
public class RedisScriptConfig {

    @Bean("seckillDecrScript")
    public DefaultRedisScript<Long> seckillDecrScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/seckill_decr.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean("budgetDecrScript")
    public DefaultRedisScript<Long> budgetDecrScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/budget_decr.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    @Bean("promoJoinReserveScript")
    public DefaultRedisScript<Long> promoJoinReserveScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/promo_join_reserve.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
