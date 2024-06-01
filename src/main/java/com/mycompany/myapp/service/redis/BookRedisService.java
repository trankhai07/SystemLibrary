package com.mycompany.myapp.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.Book;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
public class BookRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String KEY_PREFIX = "Book";
    private final String KEY_DETAIL = "BookDetail";

    public BookRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveBooksByCategoryId(long categoryId, List<Book> books) {
        try {
            String key = KEY_PREFIX + ":" + categoryId;
            redisTemplate.opsForList().rightPushAll(key, books.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean keyExists(long categoryId) {
        try {
            String key = KEY_PREFIX + ":" + categoryId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Book> getBooksByCategoryId(long categoryId) {
        try {
            String key = KEY_PREFIX + ":" + categoryId;
            List<Object> bookObjects = redisTemplate.opsForList().range(key, 0, -1);
            return (List<Book>) (List<?>) bookObjects; // Ép kiểu danh sách các đối tượng
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Book getBookCategoryById(long categoryId, long bookId) {
        String key = KEY_PREFIX + ":" + categoryId;
        Book book = (Book) redisTemplate.opsForList().index(key, bookId);
        return book;
    }

    public void deleteBooksByCategoryId(long categoryId) {
        try {
            String key = KEY_PREFIX + ":" + categoryId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // book detail
    public void saveBooks(Book book) {
        try {
            String key = KEY_DETAIL + ":" + book.getId();
            ObjectMapper mapper = new ObjectMapper();
            String bookJson = mapper.writeValueAsString(book);
            redisTemplate.opsForHash().put(key, "book", bookJson);

            // Thiết lập thời gian sống là 1 ngày (24 giờ)
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
            //            redisTemplate.expire(key, 30, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Book getBookById(long bookId) {
        try {
            String key = KEY_DETAIL + ":" + bookId;
            String bookJson = (String) redisTemplate.opsForHash().get(key, "book");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(bookJson, Book.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean keyBookIdExists(long bookId) {
        try {
            String key = KEY_DETAIL + ":" + bookId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteBooksId(long bookId) {
        try {
            String key = KEY_DETAIL + ":" + bookId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keysToDelete = (Set<String>) redisTemplate.execute(
                (RedisCallback<Set<String>>) connection -> {
                    Set<String> keys = new HashSet<>();
                    try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(prefix + "*").build())) {
                        while (cursor.hasNext()) {
                            keys.add(new String(cursor.next()));
                        }
                    }
                    return keys;
                }
            );
            assert keysToDelete != null;
            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
