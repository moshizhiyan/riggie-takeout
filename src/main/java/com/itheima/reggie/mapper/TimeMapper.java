package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Time;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.tomcat.jni.Local;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

// todo 记得删
@Mapper
public interface TimeMapper extends BaseMapper<Time> {
    @Select("select * from time where time between #{start} and #{end};")
    List<Time> timeTest(LocalDate start, LocalDate end);
}
