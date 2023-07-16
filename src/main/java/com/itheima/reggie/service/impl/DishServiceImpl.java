package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品并保存口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //要先插入菜品信息，才能有菜品id
        super.save(dishDto);

        List<DishFlavor> flavors = dishDto.getFlavors();
        //插入口味
        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(dishDto.getId());
            dishFlavorService.save(dishFlavor);
        });
    }

    /**
     * 查询菜品及对应口味
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = super.getById(id);
        //查询口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //封装成DishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * 更新菜品信息及口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        super.updateById(dishDto);

        //插入口味，先删除原来的，再添加新的
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //更新关联信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.forEach(dishFlavor -> {
            //将口味信息置为空，否则可能因为逻辑删除的原因无法插入
            dishFlavor.setId(null);
            dishFlavor.setDishId(dishDto.getId());
            dishFlavorService.save(dishFlavor);
        });
    }

    /**
     * 判断菜品是否在套餐中，能否删除
     * @param ids
     */
    @Override
    public void deleteJudgeSetmeal(List<Long> ids) {
        //判断菜品是否在某套餐内
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId, ids);

        if(setmealDishService.count(queryWrapper) == 0){
            //菜品不在套餐内，可以删除
            super.removeByIds(ids);
            return;
        }

        //菜品在套餐内，不能删除
        throw new CustomException("不能删除在套餐内的菜品");
    }
}
