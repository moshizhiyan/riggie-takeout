package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐同时保存菜品和套餐关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐的同时删除对应的套餐和菜品关联信息
     * @param ids
     */
    void deleteWithDish(List<Long> ids);

    /**
     * 回显套餐信息及其对应菜品
     * @param id
     * @return
     */
    SetmealDto getByIdWithDish(Long id);

    /**
     * 改变菜品的启售停售状态
     * @param ids
     * @return
     */
    int changeStatus(List<Long> ids, Integer status);

    /**
     * 跟新套餐信息及其对应菜品关系
     * @param setmealDto
     * @return
     */
    void updateWithDish(SetmealDto setmealDto);
}
