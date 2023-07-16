package com.reggi_take_out;

import com.itheima.reggie.ReggieApplication;
import com.itheima.reggie.entity.Time;
import com.itheima.reggie.mapper.TimeMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@SpringBootTest(classes = ReggieApplication.class)
public class MyTest {
    @Autowired
    TimeMapper timeMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test(){
        System.out.println("test");
    }

    @Test
    public void timeTest(){
        LocalDate localDate = LocalDate.of(2023, 6, 25);
//        Date start = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate start = localDate;
//        Date end = Date.from(localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate end = start.plusDays(1);
        System.out.println(start);
        System.out.println(end);
        List<Time> timeList = timeMapper.timeTest(start, end);
        System.out.println(timeList);
    }

    @Test
    public void redisTest(){
        redisTemplate.opsForValue().set("name", "xiaoming", 10l, TimeUnit.SECONDS);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object name = valueOperations.get("name");
        log.info(name.toString());

    }
}
