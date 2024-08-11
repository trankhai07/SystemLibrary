package com.mycompany.myapp.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.PatronAccount;
import com.mycompany.myapp.service.dto.InfoCheckOut;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WaitListRedisService {

    private final String KEY_PREFIX = "WaitList";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public WaitListRedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void add(Book book, PatronAccount patronAccount) {
        try {
            String key = KEY_PREFIX + ":" + book.getId();
            String setKey = KEY_PREFIX + ":set:" + book.getId();
            InfoCheckOut infoCheckOut = new InfoCheckOut();
            infoCheckOut.setBookTitle(book.getTitle());
            infoCheckOut.setEmail(patronAccount.getUser().getEmail());
            infoCheckOut.setUsername(patronAccount.getUser().getLogin());
            String infoCheckOutJson = objectMapper.writeValueAsString(infoCheckOut);

            Boolean isMember = redisTemplate.opsForSet().isMember(setKey, infoCheckOutJson);
            if (Boolean.FALSE.equals(isMember)) {
                redisTemplate.opsForList().rightPush(key, infoCheckOutJson);
                redisTemplate.opsForSet().add(setKey, infoCheckOutJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<InfoCheckOut> getAll(long bookId) {
        try {
            String key = KEY_PREFIX + ":" + bookId;
            List<Object> jsonList = redisTemplate.opsForList().range(key, 0, -1);
            List<InfoCheckOut> infoCheckOutList = new ArrayList<>();
            assert jsonList != null;
            for (Object json : jsonList) {
                InfoCheckOut infoCheckOut = objectMapper.readValue(json.toString(), InfoCheckOut.class);
                infoCheckOutList.add(infoCheckOut);
            }
            return infoCheckOutList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public InfoCheckOut poll(Book book) {
        try {
            String key = KEY_PREFIX + ":" + book.getId();

            String json = (String) redisTemplate.opsForList().leftPop(key.trim());
            if (json != null) {
                InfoCheckOut infoCheckOut = objectMapper.readValue(json, InfoCheckOut.class);
                return infoCheckOut;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void delete(long bookId) {
        try {
            String key = KEY_PREFIX + ":" + bookId;
            String setKey = KEY_PREFIX + ":set:" + bookId;
            redisTemplate.delete(setKey);
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
