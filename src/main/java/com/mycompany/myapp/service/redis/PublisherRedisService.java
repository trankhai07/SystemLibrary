package com.mycompany.myapp.service.redis;

import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.Publisher;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String KEY_PREFIX = "Publisher";

    public PublisherRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void savePublisherToRedis(List<Publisher> publishers) {
        try {
            redisTemplate.opsForList().rightPushAll(KEY_PREFIX, publishers.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean keyExists() {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Publisher> getPublisherFromRedis() {
        try {
            List<Object> publisherObjects = redisTemplate.opsForList().range(KEY_PREFIX, 0, -1);
            return (List<Publisher>) (List<?>) publisherObjects; // Ép kiểu danh sách các đối tượng
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deletePublisherFromRedis() {
        try {
            redisTemplate.delete(KEY_PREFIX);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
