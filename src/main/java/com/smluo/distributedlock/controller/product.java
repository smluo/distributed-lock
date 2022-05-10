package com.smluo.distributedlock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/product")
public class product {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_KEY = "LOCK_KEY";

    @GetMapping("/sale")
    public String sale() throws InterruptedException {
        String clientId = UUID.randomUUID().toString();
        Boolean lockKey = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, clientId, 10L, TimeUnit.SECONDS);
        while (!lockKey) {
            Thread.sleep(500);
            lockKey = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, clientId, 10L, TimeUnit.SECONDS);
        }
        try {
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                stock--;
                stringRedisTemplate.opsForValue().set("stock", String.valueOf(stock));
                System.out.printf("扣减成功，剩余库存：%d\n", stock);
            } else {
                System.out.println("库存为空");
            }
            return "success";
        } finally {
            stringRedisTemplate.delete(LOCK_KEY);
        }
    }
}
