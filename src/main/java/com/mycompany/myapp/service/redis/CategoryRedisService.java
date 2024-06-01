package com.mycompany.myapp.service.redis;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.domain.Publisher;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CategoryRedisService {

    private final String KEY_PREFIX = "Category";
    private final RedisTemplate<String, Object> redisTemplate;

    public CategoryRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveCategoryToRedis(List<Category> categoryList) {
        try {
            redisTemplate.opsForList().rightPushAll(KEY_PREFIX, categoryList.toArray());
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

    public List<Category> getCategoryFromRedis() {
        try {
            List<Object> categoryObjects = redisTemplate.opsForList().range(KEY_PREFIX, 0, -1);
            return (List<Category>) (List<?>) categoryObjects; // Ép kiểu danh sách các đối tượng
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteCategoryFromRedis() {
        try {
            redisTemplate.delete(KEY_PREFIX);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
