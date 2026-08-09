package com.avocado.global.config;

import com.avocado.global.config.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;


@SpringJUnitConfig(classes = RedisConfig.class)
class RedisConfigTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("RedisConnectionFactory Bean이 정상적으로 생성된다")
    void redisConnectionFactory() {
        assertThat(redisConnectionFactory).isNotNull();
    }

    @Test
    @DisplayName("StringRedisTemplate Bean이 정상적으로 생성된다")
    void stringRedisTemplate() {
        assertThat(stringRedisTemplate).isNotNull();
    }

    @Test
    @DisplayName("Redis 서버와 정상적으로 연결된다")
    void redisConnection() {
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {

            // Redis에 PING 명령을 전송하여 실제 연결 상태 확인
            String response = conn.ping();

            assertThat(response).isEqualTo("PONG");
        }
    }

    @Test
    @DisplayName("문자열 데이터를 저장하고 조회할 수 있다")
    void setAndGet() {
        String key = "test:redis";
        String value = "hello";

        stringRedisTemplate.opsForValue()
                .set(key, value);

        assertThat(stringRedisTemplate.opsForValue().get(key)).isEqualTo(value);

        stringRedisTemplate.delete(key);
    }

    @Test
    @DisplayName("저장된 데이터를 삭제할 수 있다")
    void delete() {
        String key = "test:redis";
        String value = "hello";

        stringRedisTemplate.opsForValue()
                .set(key, value);

        Boolean deleted = stringRedisTemplate.delete(key);

        assertThat(deleted).isTrue();

        assertThat(stringRedisTemplate.opsForValue().get(key)).isNull();
    }

    @Test
    @DisplayName("데이터에 만료 시간을 설정할 수 있다")
    void expiration() {
        String key = "test:redis:ttl";

        stringRedisTemplate.opsForValue().set(
                key,
                "hello",
                3,
                TimeUnit.MINUTES
        );

        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        assertThat(ttl)
                .isNotNull()
                .isPositive()
                .isLessThanOrEqualTo(180);

        stringRedisTemplate.delete(key);
    }
}