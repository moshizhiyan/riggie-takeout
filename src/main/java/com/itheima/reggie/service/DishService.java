package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品并插入口味
    void saveWithFlavor(DishDto dishDto);

    //查询菜品及对应口味
    DishDto getByIdWithFlavor(Long id);

    //修改菜品及口味信息
    void updateWithFlavor(DishDto dishDto);

    /**
     * 判断菜品是否在套餐中，能否删除
     * @param ids
     */
    void deleteJudgeSetmeal(List<Long> ids);
}
