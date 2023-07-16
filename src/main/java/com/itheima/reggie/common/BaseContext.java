package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取用户id
 */
@Slf4j
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
//        log.info("the id is: "+id);
    }

    public static Long getCurrentId(){
//        log.info("id is: "+ threadLocal.get());
        return threadLocal.get();
    }
}
