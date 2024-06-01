package com.mycompany.myapp.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.CheckOut;
import com.mycompany.myapp.service.dto.InfoCheckOut;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CheckOutRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String KEY_PREFIX = "CheckOut";

    public CheckOutRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveCheckOutByPatron(CheckOut checkOut) {
        try {
            String key = KEY_PREFIX + ":" + checkOut.getPatronAccount().getCardNumber() + ":" + checkOut.getId();
            String keySub = key + ":Sub";
            InfoCheckOut infoCheckOut = new InfoCheckOut();
            infoCheckOut.setEmail(checkOut.getPatronAccount().getUser().getEmail());
            infoCheckOut.setUsername(checkOut.getPatronAccount().getUser().getLogin());
            infoCheckOut.setBookTitle(checkOut.getBookCopy().getBook().getTitle());
            ObjectMapper mapper = new ObjectMapper();
            String infoCheckOutJson = mapper.writeValueAsString(infoCheckOut);
            redisTemplate.opsForHash().put(key, "infoCheckOut", infoCheckOutJson);

            Duration duration = Duration.between(checkOut.getStartTime(), checkOut.getEndTime());
            long hours = duration.toHours();
            redisTemplate.expire(key, hours, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(keySub, "check", hours - 20, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InfoCheckOut getCheckOutDataByKey(String key) {
        try {
            String infoCheckOutJson = (String) redisTemplate.opsForHash().get(key, "infoCheckOut");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(infoCheckOutJson, InfoCheckOut.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteCheckOutByKey(CheckOut checkOut) {
        try {
            String key = KEY_PREFIX + ":" + checkOut.getPatronAccount().getCardNumber() + ":" + checkOut.getId();
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
