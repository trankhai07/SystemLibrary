package com.mycompany.myapp.service.redis;

import com.mycompany.myapp.service.MailService;
import com.mycompany.myapp.service.dto.InfoCheckOut;
import java.util.Map;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RedisExpiredKeyListener {

    private final CheckOutRedisService checkOutRedisService;
    private final MailService mailService;

    public RedisExpiredKeyListener(CheckOutRedisService checkOutRedisService, MailService mailService) {
        this.checkOutRedisService = checkOutRedisService;
        this.mailService = mailService;
    }

    @Async
    public void handleMessage(String message) {
        System.out.println("Key expired: " + message);
        String keySub = message.trim();
        String[] keys = keySub.split(":");
        if (keys[0].equals("CheckOut") && keys.length > 3) {
            String key = keys[0] + ":" + keys[1] + ":" + keys[2];
            InfoCheckOut infoCheckOut = checkOutRedisService.getCheckOutDataByKey(key);
            if (infoCheckOut.getEmail() != null && infoCheckOut.getBookTitle() != null) {
                System.out.println("check test mail");
                mailService.sendReturnBook(infoCheckOut);
            }
        }
    }
}
